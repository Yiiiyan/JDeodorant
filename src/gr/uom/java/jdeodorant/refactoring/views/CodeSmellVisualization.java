package gr.uom.java.jdeodorant.refactoring.views;

import gr.uom.java.ast.visualization.FeatureEnvyDiagram;
import gr.uom.java.ast.visualization.FeatureEnvyVisualizationData;
import gr.uom.java.ast.visualization.GodClassDiagram2;
import gr.uom.java.ast.visualization.GodClassVisualizationData;
import gr.uom.java.ast.visualization.InputAction;
import gr.uom.java.ast.visualization.VisualizationData;
import gr.uom.java.ast.visualization.ZoomAction;
import gr.uom.java.jdeodorant.refactoring.Activator;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

public class CodeSmellVisualization extends ViewPart {
	
	public static final String ID = "gr.uom.java.jdeodorant.views.CodeSmellVisualization";
	private FigureCanvas figureCanvas; 
	private ScalableFreeformLayeredPane root = null;

	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());

		figureCanvas = new FigureCanvas(parent, SWT.DOUBLE_BUFFERED);
		figureCanvas.setBackground(ColorConstants.white);

		VisualizationData data = CodeSmellVisualizationDataSingleton.getData();

		if(data != null) {
			if(data instanceof GodClassVisualizationData) {
				GodClassDiagram2 diagram = new GodClassDiagram2((GodClassVisualizationData)data);
				root= diagram.getRoot();
			}
			if(data instanceof FeatureEnvyVisualizationData) {
				FeatureEnvyDiagram diagram = new FeatureEnvyDiagram((FeatureEnvyVisualizationData)data);
				root= diagram.getRoot();
			}

			figureCanvas.setViewport(new FreeformViewport());
			MouseWheelListener listener = new MouseWheelListener() {
				private double scale;
				private static final double ZOOM_INCRENENT = 0.1;
				private static final double ZOOM_DECREMENT = 0.1;

				private void zoom(int count) {
					if (count > 0) {
						scale += ZOOM_INCRENENT;

					} else {
						scale -= ZOOM_DECREMENT;
					}

					if (scale <= 0) {
						scale = 0;
					}

					root.setScale(scale);
				}

				public void mouseScrolled(MouseEvent e) {
					scale = root.getScale();
					int count = e.count;
					zoom(count);
				}
			};

			figureCanvas.setContents(root);
			figureCanvas.addMouseWheelListener(listener);

		}

		// Custom Action for the View's Menu  
		ImageDescriptor imageDescriptor = Activator.getImageDescriptor("/icons/" + "magnifier.png");
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		Action act=new Action("Zoom",SWT.DROP_DOWN){};
		act.setImageDescriptor(imageDescriptor);
		act.setMenuCreator(new MyMenuCreator());
		manager.add(act);
	}


	class MyMenuCreator implements IMenuCreator{

		private IAction action;
		private Menu menu;

		public void selectionChanged(IAction action, ISelection selection)
		{
			if (action != this.action)
			{
				action.setMenuCreator(this);
				this.action = action;
			}
		} 

		public Menu getMenu(Control ctrl){
			Menu menu = new Menu(ctrl);
			addActionToMenu(menu, newZoomAction(0.5));
			addActionToMenu(menu, newZoomAction(1));
			addActionToMenu(menu, newZoomAction(2));
			addActionToMenu(menu, newZoomAction(0));

			InputAction inputZoomAction = new InputAction(root);
			inputZoomAction.setText("Other...");

			addActionToMenu(menu, inputZoomAction);
			return menu;

		}

		public void dispose() {
			if (menu != null)
			{
				menu.dispose();
			}
		}

		public Menu getMenu(Menu parent) {
			return null;
		}

		private void addActionToMenu(Menu menu, IAction action)
		{
			ActionContributionItem item= new ActionContributionItem(action);
			item.fill(menu, -1);
		}
	}

	public void setFocus() {

	}

	public ZoomAction newZoomAction(double scale){
		ZoomAction zoomAction = new ZoomAction(root, scale);
		if(scale != 0){
			double percent = scale*100;
			zoomAction.setText((int) percent +"%");
			zoomAction.setImageDescriptor(Activator.getImageDescriptor("/icons/" + "magnifier.png"));
		}else
			zoomAction.setText("Scale to Fit");
		return zoomAction;
	}

}