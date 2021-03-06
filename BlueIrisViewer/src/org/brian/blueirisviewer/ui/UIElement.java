package org.brian.blueirisviewer.ui;

import org.brian.blueirisviewer.BlueIrisViewer;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public abstract class UIElement
{
	private Table parent;
	private Window window;
	private Table table;

	public UIElement(Skin skin)
	{
		UI.uiElements.add(this);
		parent = new Table();
		parent.setFillParent(true);
		window = new Window("Unnamed Window", skin);
		window.setModal(BlueIrisViewer.bivSettings.modalUI);
		table = new Table(skin);
		window.add(table);

		hide();

		parent.add(window);
		onCreate(skin, window, table);
	}

	public void show()
	{
		if (parent != null && !parent.hasParent())
		{
			UI.root.addActor(parent);
			onShow();
		}
	}

	public void hide()
	{
		if (parent != null)
		{
			parent.remove();
			onHide();
		}
	}

	public boolean isShowing()
	{
		return parent != null && parent.hasParent();
	}

	public void doUpdate()
	{
		if (isShowing())
			onUpdate(window, table);
	}

	public void setModal(boolean isModal)
	{
		window.setModal(isModal);
	}
	/**
	 * Can be overridden by an inheriting class to be notified when the UI element is shown.
	 */
	protected void onShow()
	{
	}
	/**
	 * Can be overridden by an inheriting class to be notified when the UI element is hidden.
	 */
	protected void onHide()
	{
	}

	protected abstract void onCreate(final Skin skin, final Window window, final Table table);

	protected abstract void onUpdate(final Window window, final Table table);

	protected abstract void onDestroy();
}
