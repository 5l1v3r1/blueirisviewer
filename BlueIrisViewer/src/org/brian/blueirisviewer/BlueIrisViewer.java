package org.brian.blueirisviewer;

import org.brian.blueirisviewer.images.Images;
import org.brian.blueirisviewer.ui.MainOptionsWnd;
import org.brian.blueirisviewer.ui.UI;
import org.brian.blueirisviewer.ui.WindowOptionsWnd;
import org.brian.blueirisviewer.util.IntPoint;
import org.brian.blueirisviewer.util.IntRectangle;
import org.brian.blueirisviewer.util.Utilities;
import org.brian.blueirisviewer.util.WindowHelper;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BlueIrisViewer implements ApplicationListener
{
	private static Object resizeLock = new Object();

	public static Object getResizeLock()
	{
		return resizeLock;
	}

	public boolean restart = false;

	private OrthographicCamera camera;
	// private OrthographicCamera pixelPerfectCamera;
	private SpriteBatch batch;

	public static float fScreenWidth = 1;
	public static float fScreenHeight = 1;
	public static int iScreenWidth = 1;
	public static int iScreenHeight = 1;

	public static Images images;
	public static UI ui;
	public static BIVSettings bivSettings;

	private long lastResize = 0;
	private long lastHandledResize = 0;
	private boolean isDragging = false;
	private long skipThisResize = 0;

	public static WindowHelper windowHelper;

	public BlueIrisViewer(WindowHelper windowHelper)
	{
		this.windowHelper = windowHelper;
	}

	@Override
	public void create()
	{
		// DisplayMode[] dms = Gdx.graphics.getDisplayModes();
		Texture.setEnforcePotImages(false);

		bivSettings = new BIVSettings();
		bivSettings.Load();
		float w = fScreenWidth = iScreenWidth = Gdx.graphics.getWidth();
		float h = fScreenHeight = iScreenHeight = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w, h);
		camera.setToOrtho(false, w, h);
		camera.update();

		batch = new SpriteBatch();

		Gdx.input.setInputProcessor(myInputProcessor);

		ui = new UI();

		if (bivSettings.restartBorderlessToggle)
		{
			bivSettings.restartBorderlessToggle = false;
			bivSettings.Save();
			ui.openWindow(MainOptionsWnd.class);
			ui.openWindow(WindowOptionsWnd.class);
		}

		images = new Images();
		images.Initialize();
	}

	@Override
	public void dispose()
	{
		images.dispose();
		ui.dispose();
		batch.dispose();
	}

	@Override
	public void render()
	{
		if (bivSettings.restartBorderlessToggle)
		{
			BlueIrisViewer.bivSettings.Save();
			restart = true;
			Gdx.app.exit();
		}
		if (lastHandledResize != lastResize && skipThisResize != lastResize
				&& lastResize + 250 < GameTime.getRealTime())
		{
			// Last resize was at least 100 ms ago, and we haven't yet handled it by setting the DisplayMode.
			lastHandledResize = lastResize;
			if (windowHelper != null)
				windowHelper.SetWindowRectangle(windowHelper.GetWindowRectangle());
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		// Update GameTime
		GameTime.tick();

		images.render(batch);

		batch.end();
		ui.render();
	}

	@Override
	public void resize(int w, int h)
	{
		synchronized (resizeLock)
		{
			System.out.println(w + "x" + h + " isDragging: " + isDragging);
			if (!isDragging && !bivSettings.disableWindowDragging && !bivSettings.borderless)
			{
				lastResize = GameTime.getRealTime();
				if (skipThisResize == 0)
					skipThisResize = lastResize;
			}

			fScreenWidth = iScreenWidth = w;
			fScreenHeight = iScreenHeight = h;

			images.resize(w, h);
			ui.resize(w, h);

			camera.setToOrtho(false, w, h);
			camera.update();
		}
	}

	@Override
	public void pause()
	{
		GameTime.pause();
		if (windowHelper != null && bivSettings != null && !bivSettings.disableWindowDragging)
		{
			IntRectangle currentPosition = windowHelper.GetWindowRectangle();
			bivSettings.startPositionX = currentPosition.x;
			bivSettings.startPositionY = currentPosition.y;
			bivSettings.startSizeW = currentPosition.width;
			bivSettings.startSizeH = currentPosition.height;
			bivSettings.Save();
		}
	}

	@Override
	public void resume()
	{
		GameTime.unpause();
	}

	public InputProcessor myInputProcessor = new InputProcessor()
	{
		IntRectangle storedPosition = new IntRectangle(0, 0, 1, 1);
		int downX = 0;
		int downY = 0;
		long nextMove = Long.MIN_VALUE;
		int lastDownImageId = -1;
		boolean movedSinceLastDown = false;

		@Override
		public boolean keyDown(int keycode)
		{
			return ui.stage.keyDown(keycode);
		}

		@Override
		public boolean keyUp(int keycode)
		{
			return ui.stage.keyUp(keycode);
		}

		@Override
		public boolean keyTyped(char character)
		{
			if (ui.stage.keyTyped(character))
				return true;
			if (character == 'o' || character == 'O')
			{
				ui.openWindow(MainOptionsWnd.class);
				return true;
			}
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button)
		{
			if (ui.stage.touchDown(screenX, screenY, pointer, button))
				return true;
			isDragging = true;
			if (pointer == 0)
			{
				if (windowHelper != null)
				{
					storedPosition = windowHelper.GetWindowRectangle();
					downX = screenX + storedPosition.x;
					downY = screenY + storedPosition.y;
					nextMove = Utilities.getTimeInMs() + 200;
					movedSinceLastDown = false;
				}
				int col = (int) (screenX / images.getImageWidth());
				int row = (int) (screenY / images.getImageHeight());
				lastDownImageId = (row * images.getColCount()) + col;
			}
			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button)
		{
			isDragging = false;
			if (ui.stage.touchUp(screenX, screenY, pointer, button))
				return true;

			if (!movedSinceLastDown)
			{
				if (images.getFullScreenedImageId() == -1)
				{
					int col = (int) (screenX / images.getImageWidth());
					int row = (int) (screenY / images.getImageHeight());
					if (lastDownImageId == (row * images.getColCount()) + col
							&& lastDownImageId < images.getNumImages())
						images.setFullScreenedImageId(lastDownImageId);
				}
				else
					images.setFullScreenedImageId(-1);
			}
			return true;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer)
		{
			if (ui.stage.touchDragged(screenX, screenY, pointer))
				return true;
			long timeNow = Utilities.getTimeInMs();
			if (pointer == 0 && windowHelper != null && timeNow > nextMove && isDragging)
			{
				// If we allow moves too fast, it won't give the window time to move from the
				// last event and everything goes wrong.
				// This does cause movement to be choppy, but safe.
				nextMove = timeNow + 16;
				if (!bivSettings.disableWindowDragging)
				{
					IntRectangle currentPosition = windowHelper.GetWindowRectangle();
					int currentX = screenX + currentPosition.x;
					int currentY = screenY + currentPosition.y;
					int dx = currentX - downX;
					int dy = currentY - downY;
					windowHelper.SetWindowPosition(new IntPoint(storedPosition.x + dx, storedPosition.y + dy));
				}
				movedSinceLastDown = true;
			}
			return true;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY)
		{
			return ui.stage.mouseMoved(screenX, screenY);
		}

		@Override
		public boolean scrolled(int amount)
		{
			return ui.stage.scrolled(amount);
		}
	};
}