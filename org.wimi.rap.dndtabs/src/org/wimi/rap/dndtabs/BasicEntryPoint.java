package org.wimi.rap.dndtabs;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class BasicEntryPoint extends AbstractEntryPoint
{
	private static final long serialVersionUID = 1L;

	private static final String DRAGED_ITEM_INDEX = "dragedItemIndex";
	private static final String SOURCE_FOLDER = "sourceFolder";
	private static final String IS_SHOWING = "isShowing";
	private static final String ITEM_CAPTION = "itemCaption";

	@Override
	protected void createContents(Composite parent)
	{
		GridLayout gridLayout = new GridLayout(1, false);
		parent.setLayout(gridLayout);

		Composite tabComposite = createTabs(parent);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tabComposite.setLayoutData(gridData);
	}

	private Composite createTabs(Composite parent)
	{
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new FillLayout());

		SashForm sash_form = new SashForm(root, SWT.HORIZONTAL);

		CTabFolder cupperTab = new CTabFolder(sash_form, SWT.NONE);
		addItems(cupperTab, "left");
		cupperTab.setSelection(0);

		addDragListener(cupperTab);
		addDropListener(cupperTab);

		CTabFolder clowerTab = new CTabFolder(sash_form, SWT.NONE);
		addItems(clowerTab, "right");
		clowerTab.setSelection(0);

		addDragListener(clowerTab);
		addDropListener(clowerTab);

		return root;
	}

	private void addItems(CTabFolder folder, String titlePrefix)
	{
		CTabItem item = null;
		for (int i = 0; i < 3; i++)
		{
			item = new CTabItem(folder, SWT.CLOSE);
			item.setText(titlePrefix + " " + i);

			Composite composite = new Composite(folder, SWT.NONE);
			composite.setLayout(new FillLayout());
			Text text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.VERTICAL);
			String t = "This is the content of CTabItem '" + titlePrefix + " " + i + "'\n";
			text.setText(t + t + t);
			item.setControl(composite);
		}
	}

	private void addDragListener(final CTabFolder folder)
	{
		final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

		final DragSourceAdapter dragAdapter = new DragSourceAdapter()
		{
			private static final long serialVersionUID = 1L;

			private CTabItem item;

			@Override
			public void dragStart(DragSourceEvent event)
			{
				event.doit = folder.getItemCount() != 0;
				Point cursorLocation = folder.getDisplay().getCursorLocation();
				item = folder.getItem(folder.toControl(cursorLocation));
				event.doit = item != null;
			}

			@Override
			public void dragFinished(DragSourceEvent event)
			{
				super.dragFinished(event);
			}

			@Override
			public void dragSetData(final DragSourceEvent event)
			{
				Control control = item.getControl();
				control.setData(ITEM_CAPTION, item.getText());
				control.setData(IS_SHOWING, folder.getSelectionIndex() == folder.indexOf(item));
				control.setData(SOURCE_FOLDER, folder);
				control.setData(DRAGED_ITEM_INDEX, folder.indexOf(item));

				transfer.setSelection(new StructuredSelection(control));

				item.setControl(null);
				item.dispose();
			}
		};

		final DragSource dragSource = new DragSource(folder, DND.DROP_MOVE);
		dragSource.setTransfer(new Transfer[] {transfer});
		dragSource.addDragListener(dragAdapter);
	}

	private void addDropListener(final CTabFolder folder)
	{
		final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

		final DropTargetAdapter dragAdapter = new DropTargetAdapter()
		{
			private static final long serialVersionUID = 1L;

			private int indexToInsert;

			@Override
			public void dragOver(DropTargetEvent event)
			{
				if (folder.getDisplay().getCursorControl() instanceof CTabFolder)
				{
					event.detail = DND.DROP_MOVE;
					CTabItem itemUnderCursor = folder.getItem(folder.toControl(folder.getDisplay().getCursorLocation()));
					indexToInsert = folder.indexOf(itemUnderCursor);
					if (itemUnderCursor == null)
					{
						// insert at the end
						indexToInsert = folder.getItemCount() - 1;
					}
					else
					{
						Rectangle bounds = itemUnderCursor.getBounds();

						int x = folder.toControl(event.x, event.y).x;
						int middle = bounds.x + ( bounds.width / 2 );

						if (x <= middle)
						{
							// insert before this tab
							indexToInsert--;
						}
					}
				}
				else
				{
					event.detail = DND.DROP_NONE;
				}
			}

			@Override
			public void drop(final DropTargetEvent event)
			{
				Control droppedObj = (Control) ( (StructuredSelection) transfer.getSelection() ).getFirstElement();
				Object source = droppedObj.getData(SOURCE_FOLDER);
				final String itemCaption = (String) droppedObj.getData(ITEM_CAPTION);
				int dragedItemIndex = (int) droppedObj.getData(DRAGED_ITEM_INDEX);

				if (source == folder)
				{
					// dnd between same folder, old item is already disposed so item under cursorlocation is wrong
					if (indexToInsert < dragedItemIndex)
					{
						// move from right to left
						indexToInsert++;
					}
				}
				else
				{
					// dnd between different folders
					indexToInsert++;
				}
				createNewItem(folder, indexToInsert, droppedObj, itemCaption, (boolean) droppedObj.getData(IS_SHOWING));
			}

			private CTabItem createNewItem(final CTabFolder folder, int index, final Control droppedObj, final String itemCaption,
				boolean select)
			{
				CTabItem item = new CTabItem(folder, SWT.CLOSE, index);
				item.setText(itemCaption);
				droppedObj.setParent(folder);
				item.setControl(droppedObj);
				if (select)
				{
					folder.setSelection(item);
				}
				return item;
			}
		};

		final DropTarget dropTarget = new DropTarget(folder, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] {transfer});
		dropTarget.addDropListener(dragAdapter);
	}
}
