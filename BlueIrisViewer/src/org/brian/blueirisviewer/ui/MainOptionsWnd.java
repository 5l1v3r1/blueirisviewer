package org.brian.blueirisviewer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MainOptionsWnd extends UIElement
{
	public MainOptionsWnd(Skin skin)
	{
		super(skin);
	}

	@Override
	public void onCreate(final Skin skin, final Window window, final Table table)
	{
		table.columnDefaults(0).align(Align.center).padRight(20);
		table.columnDefaults(1).align(Align.center);
		table.pad(10, 10, 10, 10);

		window.setTitle("BlueIrisView Options");

		AddWindowButton(new ServerSetupWnd(skin), "Server Setup", skin, table);
		AddWindowButton(new InstantReplayWnd(skin), "Instant Replay", skin, table);

		table.row();
		table.add().height(10);
		table.row();

		AddWindowButton(new WindowOptionsWnd(skin), "Window Options", skin, table);
		AddWindowButton(new NightModeWnd(skin), "Night Mode", skin, table);

		table.row();
		table.add().height(10);
		table.row();

		AddWindowButton(new CameraLayoutWnd(skin), "Camera Layout", skin, table);
		AddWindowButton(new FreezeDetectionWnd(skin), "Freeze Detection", skin, table);

		table.row();
		table.add().height(10);
		table.row();

		AddWindowButton(new PerformanceWnd(skin), "Performance", skin, table);
		AddWindowButton(new AboutWnd(skin), "About", skin, table);

		table.row();
		table.add().height(20);
		table.row();

		final TextButton btnExit = new TextButton("Exit App", skin);
		btnExit.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				UI.showYesNoQuestionDialog("Do you want to close BlueIrisViewer?", "Confirm Exit", new Runnable()
				{
					public void run()
					{
						Gdx.app.exit();
					}
				}, null);
			}
		});
		table.add(btnExit).colspan(2).align(Align.center);
		table.row();

		table.add().height(20);
		table.row();

		final TextButton btnClose = new TextButton("Close Main Menu", skin);
		btnClose.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				hide();
			}
		});
		table.add(btnClose).colspan(2).align(Align.center);
		table.row();
	}

	private void AddWindowButton(final UIElement window, String buttonText, Skin skin, Table table)
	{
		final TextButton btn = new TextButton(buttonText, skin);

		btn.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if (window.isShowing())
					window.hide();
				else
					window.show();
			}
		});

		table.add(btn);
	}

	@Override
	public void onUpdate(final Window window, final Table table)
	{
	}

	@Override
	public void onDestroy()
	{
	}
}
