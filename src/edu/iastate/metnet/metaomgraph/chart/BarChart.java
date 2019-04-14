package edu.iastate.metnet.metaomgraph.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultFormatterFactory;

import org.jcolorbrewer.ColorBrewer;
import org.jcolorbrewer.ui.ColorPaletteChooserDialog;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierList;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import edu.iastate.metnet.metaomgraph.AnimatedSwingWorker;
import edu.iastate.metnet.metaomgraph.ComponentToImage;
import edu.iastate.metnet.metaomgraph.GraphFileFilter;
import edu.iastate.metnet.metaomgraph.IconTheme;
import edu.iastate.metnet.metaomgraph.MetaOmAnalyzer;
import edu.iastate.metnet.metaomgraph.MetaOmGraph;
import edu.iastate.metnet.metaomgraph.MetaOmProject;
import edu.iastate.metnet.metaomgraph.Metadata.MetadataQuery;
import edu.iastate.metnet.metaomgraph.ui.BlockingProgressDialog;
import edu.iastate.metnet.metaomgraph.ui.TreeSearchQueryConstructionPanel;
import edu.iastate.metnet.metaomgraph.utils.Utils;

public class BarChart extends JInternalFrame implements ChartMouseListener, ActionListener {

	// hasmap mapping feature num to expression data or datacol to sample depending
	// on the box plot
	HashMap<Integer, double[]> plotData;
	// plot type 0 for feature 1 for sample
	int plotType;
	String[] rowNames;
	private MetaOmProject myProject;
	private String xAxisname;
	private ChartToolBar myToolbar;
	private ChartPanel chartPanel;
	private JFreeChart myChart;

	// private XYLineAndShapeRenderer myRenderer;
	private BoxAndWhiskerRenderer myRenderer;
	JScrollPane scrollPane;

	// toolbar buttons
	private JButton properties;
	private JButton save;
	private JButton print;
	private JButton zoomIn;
	private JButton zoomOut;
	private JButton defaultZoom;
	private JButton changePalette;
	private JButton splitDataset;
	private JButton boxPlotOptions;

	
	

	// bottom toolbar
	private JButton btnNewButton_1;

	public static final String ZOOM_IN_COMMAND = "zoomIn";
	public static final String ZOOM_OUT_COMMAND = "zoomOut";
	public static final String ZOOM_DEFAULT_COMMAND = "defaultZoom";

	// chart colors
	private Color chartbg = MetaOmGraph.getChartBackgroundColor();
	private Color plotbg = MetaOmGraph.getPlotBackgroundColor();

	Color[] colorArray = null;

	DefaultBoxAndWhiskerCategoryDataset initdataset;
	String splitCol;
	List<String> seriesNames;
	Map<String, Collection<Integer>> splitIndex;
	// to keep an order for the dataset
	List<String> orderedKeys;

	private boolean[] excludedCopy;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

	}

	/**
	 * Create the frame.
	 */
	public BarChart(HashMap<Integer, double[]> plotData, int pType, MetaOmProject mp) {

		this.plotData = plotData;
		this.plotType = pType;
		myProject = mp;
		if (plotType == 0) {
			// make copy of excluded
			boolean[] excluded = MetaOmAnalyzer.getExclude();
			if (excluded != null) {
				excludedCopy = new boolean[excluded.length];
				System.arraycopy(excluded, 0, excludedCopy, 0, excluded.length);
			}
		}
		chartPanel = null;
		// init rownames
		rowNames = initRowNames(plotData.keySet(), pType);
		// JOptionPane.showMessageDialog(null, Arrays.toString(rowNames));
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		getContentPane().add(panel, BorderLayout.NORTH);
		JButton btnNewButton = new JButton("New button");
		// panel.add(btnNewButton);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		btnNewButton_1 = new JButton("Change X axis");
		btnNewButton_1.setActionCommand("chooseX");
		btnNewButton_1.addActionListener(this);
		// panel_1.add(btnNewButton_1);

		scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		// create sample plot
		initdataset = createDataset();

		try {
			chartPanel = makeBarChart(initdataset);
		} catch (IOException e) {
		}
		scrollPane.setViewportView(chartPanel);

		// add buttons to top panel
		IconTheme theme = MetaOmGraph.getIconTheme();
		properties = new JButton(theme.getProperties());
		properties.setToolTipText("Chart Properties");
		save = new JButton(theme.getSaveAs());
		save.setToolTipText("Save Chart as Image");
		print = new JButton(theme.getPrint());
		print.setToolTipText("Print Chart");
		zoomIn = new JButton(theme.getZoomIn());
		zoomIn.setToolTipText("Zoom In");
		zoomOut = new JButton(theme.getZoomOut());
		zoomOut.setToolTipText("Zoom Out");
		defaultZoom = new JButton(theme.getDefaultZoom());
		defaultZoom.setToolTipText("Default Zoom");
		properties.setActionCommand("PROPERTIES");
		properties.addActionListener(chartPanel);
		save.setActionCommand("SAVE");
		save.addActionListener(chartPanel);
		print.setActionCommand("PRINT");
		print.addActionListener(chartPanel);
		zoomIn.setActionCommand("zoomIn");
		zoomIn.addActionListener(this);
		zoomOut.setActionCommand("zoomOut");
		zoomOut.addActionListener(this);
		defaultZoom.setActionCommand("defaultZoom");
		defaultZoom.addActionListener(this);

		changePalette = new JButton(theme.getPalette());
		changePalette.setToolTipText("Color Palette");
		changePalette.setActionCommand("changePalette");
		changePalette.addActionListener(this);
		changePalette.setOpaque(false);
		changePalette.setContentAreaFilled(false);
		changePalette.setBorderPainted(true);

		if (plotType == 0) {
			splitDataset = new JButton(theme.getSort());
			splitDataset.setToolTipText("Split by categories");
			splitDataset.setActionCommand("splitDataset");
			splitDataset.addActionListener(this);
		}

		boxPlotOptions = new JButton(theme.getOpts());
		boxPlotOptions.setToolTipText("Options");
		boxPlotOptions.setActionCommand("options");
		boxPlotOptions.addActionListener(this);
		

		panel.add(properties);
		panel.add(save);
		panel.add(print);
		// panel.add(zoomIn);
		// panel.add(zoomOut);
		panel.add(defaultZoom);
		if (plotType == 0) {
			panel.add(splitDataset);
		}
		panel.add(boxPlotOptions);
		panel.add(changePalette);
		

		// frame properties
		this.setClosable(true);
		// pack();
		putClientProperty("JInternalFrame.frameType", "normal");
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		String chartTitle = "Bar Chart:" + String.join(",", rowNames);
		this.setTitle(chartTitle);
	}

	public ChartPanel makeBarChart(DefaultBoxAndWhiskerCategoryDataset dataset) throws IOException {
		return null;
	}

	public ChartPanel makeBarChart(DefaultBoxAndWhiskerCategoryDataset dataset, boolean showMean, boolean showMedian,
			boolean showOutliers, boolean showFaroutliers, Color meanColor, Color medianColor, Color outlierColor,
			Color farourlierColor, int outSize, int faroutSize) throws IOException {

		//JFreeChart myChart = ChartFactory.createBoxAndWhiskerChart("BoxPlot", "Sample", "Value", dataset, true);
		//JFreeChart myChart = ChartFactory.createBarChart("BoxPlot", "Sample", "Value", dataset, true);

		// urmi add chart options
		//myRenderer = getBoxAndWhiskerRenderer(meanColor, medianColor, outlierColor, farourlierColor, showOutliers, showFaroutliers, outSize, faroutSize);
		myRenderer.setDefaultToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
		myRenderer.setFillBox(true);
		myRenderer.setMeanVisible(showMean);
		myRenderer.setMedianVisible(showMedian);

		myChart.getCategoryPlot().getDomainAxis()
				.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(1.5707963267948966D));
		myChart.getCategoryPlot().setRenderer(myRenderer);
		myChart.getCategoryPlot().setBackgroundPaint(MetaOmGraph.getPlotBackgroundColor());
		myChart.setBackgroundPaint(MetaOmGraph.getChartBackgroundColor());

		myChart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(

				CategoryLabelPositions.createUpRotationLabelPositions(1.5707963267948966D));
		ChartPanel chartPanel = new ChartPanel(myChart, Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height, 0, 0,
				Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height,
				true, true, true, true, true, true) {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(ChartPanel.SAVE_COMMAND)) {
					File destination = null;
					JFileChooser chooseDialog = new JFileChooser(Utils.getLastDir());
					chooseDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
					chooseDialog.setFileFilter(new GraphFileFilter(GraphFileFilter.PNG));
					JPanel sizer = new JPanel();
					JFormattedTextField widthField, heightField;
					JFormattedTextField.AbstractFormatter af = new JFormattedTextField.AbstractFormatter() {

						public Object stringToValue(String text) throws ParseException {
							try {
								return new Integer(text);
							} catch (NumberFormatException nfe) {
								return getFormattedTextField().getValue();
							}
						}

						public String valueToString(Object value) throws ParseException {
							if (value instanceof Integer) {
								Integer intValue = (Integer) value;
								if (intValue.intValue() < 1)
									return "1";
								else
									return ((Integer) value).intValue() + "";
							}
							return null;
						}

					};
					widthField = new JFormattedTextField(new DefaultFormatterFactory(af), new Integer(getWidth()));
					heightField = new JFormattedTextField(new DefaultFormatterFactory(af), new Integer(getHeight()));
					widthField.setColumns(4);
					heightField.setColumns(4);
					sizer.setLayout(new GridBagLayout());
					GridBagConstraints c = new GridBagConstraints();
					c.gridwidth = 3;
					c.fill = GridBagConstraints.NONE;
					sizer.add(new JLabel("Image size:"), c);
					c.gridwidth = 1;
					c.gridy = 1;
					sizer.add(new JLabel("Width:"), c);
					c.gridx = 1;
					sizer.add(widthField, c);
					c.gridx = 2;
					sizer.add(new JLabel("pixels"), c);
					c.gridx = 0;
					c.gridy = 2;
					sizer.add(new JLabel("Height:"), c);
					c.gridx = 1;
					sizer.add(heightField, c);
					c.gridx = 2;
					sizer.add(new JLabel("pixels"), c);
					chooseDialog.setAccessory(sizer);
					int returnVal = JFileChooser.APPROVE_OPTION;
					/*
					 * Continually show a file chooser until user selects a valid location, or
					 * cancels.
					 */
					boolean ready = false;
					while (!ready) {
						while (((destination == null)) && (returnVal != JFileChooser.CANCEL_OPTION)) {
							returnVal = chooseDialog.showSaveDialog(MetaOmGraph.getMainWindow());
							destination = chooseDialog.getSelectedFile();
						}
						// Did user cancel? If so, don't do anything.
						if (returnVal == JFileChooser.CANCEL_OPTION)
							return;
						// Check if file exists, prompt to overwrite if it
						// does
						String filename = destination.getAbsolutePath();
						if (!filename.substring(filename.length() - 4).equals(".png")) {
							filename += ".png";
							destination = new File(filename);
						}
						if (destination.exists()) {
							int overwrite = JOptionPane.showConfirmDialog(MetaOmGraph.getMainWindow(),
									filename + " already exists.  Overwrite?", "Overwrite File",
									JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
							if ((overwrite == JOptionPane.CANCEL_OPTION) || (overwrite == JOptionPane.CLOSED_OPTION))
								return;
							else if (overwrite == JOptionPane.YES_OPTION)
								ready = true;
							else
								destination = null; // No option
						} else
							ready = true;
					}
					final int oldDrawWidth = getMaximumDrawWidth();
					final int oldDrawHeight = getMaximumDrawHeight();
					final int newWidth = Integer.parseInt(widthField.getText());
					final int newHeight = Integer.parseInt(heightField.getText());
					final File trueDest = new File(destination.getAbsolutePath());
					setMaximumDrawWidth(newWidth);
					setMaximumDrawHeight(newHeight);
					try {
						ComponentToImage.saveAsPNG(this, trueDest, newWidth, newHeight);
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
					setMaximumDrawWidth(oldDrawWidth);
					setMaximumDrawHeight(oldDrawHeight);
				} else
					super.actionPerformed(e);
			}
		};
		chartPanel.setPreferredSize(new Dimension(800, 600));
		chartPanel.addChartMouseListener(this);
		setDefaultPalette();

		return chartPanel;

	}

	private DefaultBoxAndWhiskerCategoryDataset createDataset() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		if (ZOOM_IN_COMMAND.equals(e.getActionCommand())) {
			Point2D center = getCenterPoint();
			chartPanel.zoomInBoth(center.getX(), center.getY());
			return;
		}
		if (ZOOM_OUT_COMMAND.equals(e.getActionCommand())) {
			Point2D center = getCenterPoint();
			chartPanel.zoomOutBoth(center.getX(), center.getY());
			return;
		}
		if (ZOOM_DEFAULT_COMMAND.equals(e.getActionCommand())) {
			chartPanel.restoreAutoBounds();
			return;
		}

		if ("changePalette".equals(e.getActionCommand())) {
			ColorPaletteChooserDialog dialog = new ColorPaletteChooserDialog();
			ColorBrewer cb = null;
			dialog.setModal(true);
			dialog.show();
			if (dialog.wasOKPressed()) {
				cb = dialog.getColorPalette();
			}

			if (cb != null) {
				int numColors = seriesNames.size();
				// get color array
				colorArray = cb.getColorPalette(numColors);
				setPalette(colorArray);
			} else {
				// reset was pressed and the OK. show default colors
				colorArray = null;
				updateChart();

			}

			return;
		}

		if ("options".equals(e.getActionCommand())) {
			//TODO
		}
		if ("splitDataset".equals(e.getActionCommand())) {

			// show metadata categories
			if (MetaOmGraph.getActiveProject().getMetadataHybrid() == null) {
				JOptionPane.showMessageDialog(this, "No metadata found.");
				return;
			}
			String[] fields = MetaOmGraph.getActiveProject().getMetadataHybrid().getMetadataHeaders();
			String[] fields2 = new String[fields.length + 3];
			fields2[0] = "Reset";
			int selectedInd = 0;
			for (int i = 0; i < fields.length; i++) {
				fields2[i + 1] = fields[i];
				if (splitCol != null && splitCol.equals(fields2[i + 1])) {
					selectedInd = i + 1;
				}
			}
			fields2[fields2.length - 2] = "By Query";
			fields2[fields2.length - 1] = "More...";
			String col_val = (String) JOptionPane.showInputDialog(null, "Choose the column:\n", "Please choose",
					JOptionPane.PLAIN_MESSAGE, null, fields2, fields2[selectedInd]);
			if (col_val == null) {
				return;
			}

			if (col_val.equals("Reset")) {
				splitCol = null;
				splitIndex = null;
				// createDataset();
				updateChart();
				return;
			}

			List<String> selectedVals = new ArrayList<>();
			if (col_val.equals("More...")) {
				// display jpanel with check box
				JCheckBox[] cBoxes = new JCheckBox[fields.length];
				JPanel cbPanel = new JPanel();
				cbPanel.setLayout(new GridLayout(0, 3));
				for (int i = 0; i < fields.length; i++) {
					cBoxes[i] = new JCheckBox(fields[i]);
					cbPanel.add(cBoxes[i]);
				}
				int res = JOptionPane.showConfirmDialog(null, cbPanel, "Select categories",
						JOptionPane.OK_CANCEL_OPTION);
				if (res == JOptionPane.OK_OPTION) {
					for (int i = 0; i < fields.length; i++) {
						if (cBoxes[i].isSelected()) {
							selectedVals.add(fields[i]);
						}
					}
					splitCol = col_val;
				} else {
					return;
				}
				splitIndex = myProject.getMetadataHybrid().cluster(selectedVals);

			} else if (col_val.equals("By Query")) {
				splitCol = col_val;
				// display query panel
				final TreeSearchQueryConstructionPanel tsp = new TreeSearchQueryConstructionPanel(myProject, false);
				final MetadataQuery[] queries;
				queries = tsp.showSearchDialog();
				if (tsp.getQueryCount() <= 0) {
					System.out.println("Search dialog cancelled");
					// User didn't enter any queries
					return;
				}
				// final int[] result = new int[myProject.getDataColumnCount()];
				Collection<Integer> result = new ArrayList<>();
				List<Collection<Integer>> resList = new ArrayList<>();
				final boolean nohits;
				new AnimatedSwingWorker("Searching...", true) {
					@Override
					public Object construct() {
						ArrayList<Integer> toAdd = new ArrayList<Integer>(result.size());
						for (int i = 0; i < myProject.getDataColumnCount(); i++) {
							toAdd.add(i);
						}
						Integer[] hits = myProject.getMetadataHybrid().search(queries, tsp.matchAll());
						// remove excluded cols from list
						// urmi
						boolean[] excluded = excludedCopy;
						if (excluded != null) {
							List<Integer> temp = new ArrayList<>();
							for (Integer i : hits) {
								if (!excluded[i]) {
									temp.add(i);
								}
							}
							hits = new Integer[temp.size()];
							hits = temp.toArray(hits);
						}

						int index;
						for (index = 0; index < hits.length; index++) {
							result.add(hits[index]);
							toAdd.remove(hits[index]);
						}
						/*
						 * for (int i = 0; i < toAdd.size(); i++) { other.add(toAdd.get(i)); }
						 */
						resList.add(result);
						resList.add(toAdd);
						return null;
					}
				}.start();

				// create a split index with "hits" as one category and all others as second
				// category
				if (resList.get(0).size() < 1) {
					JOptionPane.showMessageDialog(null, "No hits found", "No hits", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				splitIndex = createSplitIndex(resList, Arrays.asList("Hits", "Other"));
			}

			else {
				// split data set by values of col_val
				selectedVals.add(col_val);
				splitCol = col_val;
				splitIndex = myProject.getMetadataHybrid().cluster(selectedVals);
			}

			// reset order
			orderedKeys = null;
			updateChart();

			return;
		}

	}

	/**
	 * create a map of name to indices
	 * 
	 * @param collList
	 * @param names
	 * @return
	 */
	private Map<String, Collection<Integer>> createSplitIndex(List<Collection<Integer>> collList, List<String> names) {
		Map<String, Collection<Integer>> res = new TreeMap();
		for (int i = 0; i < collList.size(); i++) {
			if (collList.get(i).size() > 0) {
				res.put(names.get(i), collList.get(i));
			}

		}
		return res;
	}

	private void setPalette(Color[] colors) {
		if (colors == null) {
			return;
		}
		int seriesCount = seriesNames.size();
		for (int i = 0; i < seriesCount; i++) {
			// call change series color
			changeSeriesColor(i, colors[i % colors.length]);
		}
	}

	private void setDefaultPalette() {
		ColorBrewer[] qlPalettes = ColorBrewer.getQualitativeColorPalettes(false);
		// choose default
		ColorBrewer myBrewer = qlPalettes[6];
		Color[] myFills = myBrewer.getColorPalette(seriesNames.size());
		setPalette(myFills);
	}

	private Point2D getCenterPoint() {
		JFreeChart myChart = this.myChart;
		CategoryAxis domain = myChart.getCategoryPlot().getDomainAxis();
		ValueAxis range = myChart.getCategoryPlot().getRangeAxis();

		double minx = domain.getLowerMargin();
		final double maxx = domain.getUpperMargin();
		final double miny = range.getLowerBound();
		final double maxy = range.getUpperBound();

		Point2D result = new Point2D() {
			public double getX() {
				return (maxx - maxy) / 2.0D;
			}

			public double getY() {
				return (maxy - miny) / 2;
			}

			public void setLocation(double x, double y) {
			}
		};
		return result;
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		// JOptionPane.showMessageDialog(null, "CLICKED");
		// If the user has double-clicked the legend or a point on the chart,
		// change the color of the double-clicked series.
		if (event.getTrigger().getClickCount() == 2) {
			if (event.getEntity() instanceof LegendItemEntity) {
				Comparable seriesKey = ((LegendItemEntity) event.getEntity()).getSeriesKey();
				// JOptionPane.showMessageDialog(null, "indexcol:"+seriesKey.toString());
				// int index = myChart.getXYPlot().getDataset().indexOf(seriesKey);
				// int index=seriesNames.indexOf(seriesKey.toString())+1;
				// JOptionPane.showMessageDialog(null, "SR:"+seriesNames.toString()+"::
				// "+seriesKey.toString());

				int index = seriesNames.indexOf(seriesKey.toString());
				// JOptionPane.showMessageDialog(null, "SR:"+seriesNames.toString()+"ind
				// of:"+index);
				changeSeriesColor(index);

				return;
			} else if (event.getEntity() instanceof CategoryItemEntity) {
				// changeSeriesColor(((CategoryItemEntity) event.getEntity()).get);
				//
				String ck = ((CategoryItemEntity) event.getEntity()).getColumnKey().toString();
				JOptionPane.showMessageDialog(null, "CLICKED2:" + ck);
				return;
			}
		}

		if (event.getTrigger().getClickCount() == 3) {
			reOrderGroups();
		}

	}

	@Override
	public void chartMouseMoved(ChartMouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Changes the colour of the Selected series
	 * 
	 * @param series
	 *            Selected series
	 */
	public void changeSeriesColor(int series) {
		Color oldColor = (Color) myRenderer.getSeriesPaint(series);

		Color newColor = JColorChooser.showDialog(MetaOmGraph.getMainWindow(), seriesNames.get(series) + " color",
				oldColor);
		if (newColor != null) {
			myRenderer.setSeriesPaint(series, newColor);
			// myRenderer.setpaint
		}
	}

	public void changeSeriesColor(int series, Color newColor) {
		if (newColor != null) {
			myRenderer.setSeriesPaint(series, newColor);
		}
	}

	/**
	 * call this after changing chart values
	 */
	private void updateChart() {
		// remove previous action listeners
		properties.removeActionListener(chartPanel);
		print.removeActionListener(chartPanel);
		save.removeActionListener(chartPanel);
		this.chartPanel = null;
		try {
			initdataset = createDataset();
			this.chartPanel = makeBarChart(initdataset);
			scrollPane.setViewportView(chartPanel);
			properties.addActionListener(chartPanel);
			print.addActionListener(chartPanel);
			save.addActionListener(chartPanel);

			this.repaint();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "ERRRRR");
		}

		return;
	}

	private String[] initRowNames(Set<Integer> keyset, int type) {
		int[] selectedInd = new int[keyset.size()];
		int k = 0;
		for (int i : keyset) {
			selectedInd[k++] = i;
		}

		if (type == 0) {
			return myProject.getDefaultRowNames(selectedInd);
		} else if (type == 1) {
			return myProject.getDataColumnHeaders(selectedInd);
		}

		return null;
	}

	private List<String> getSplitIndKeys() {
		if (splitIndex == null) {
			return null;
		}
		List<String> res = new ArrayList<>();
		if (orderedKeys == null) {
			res.addAll(splitIndex.keySet());
			return res;
		} else {
			return orderedKeys;
		}
	}

	private void reOrderGroups() {
		if (splitIndex == null) {
			return;
		}
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		DefaultListModel listmod = new DefaultListModel();
		List<String> thisOrder = getSplitIndKeys();
		for (String s : thisOrder) {
			listmod.addElement(s);
		}
		JList list = new JList<>(listmod);
		list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setTransferHandler(new ListItemTransferHandler());
		list.setDropMode(DropMode.INSERT);
		list.setDragEnabled(true);
		listPanel.add(new JScrollPane(list), BorderLayout.CENTER);

		int opt = JOptionPane.showConfirmDialog(null, listPanel, "Reorder using Drag-and-Drop",
				JOptionPane.PLAIN_MESSAGE);

		if (opt == JOptionPane.OK_OPTION) {
			Map<String, Collection<Integer>> temp = new HashMap<>();
			orderedKeys = new ArrayList<>();
			for (int i = 0; i < list.getModel().getSize(); i++) {
				// JOptionPane.showMessageDialog(null,list.getModel().getElementAt(i));
				// temp.put((String) list.getModel().getElementAt(i),
				// splitIndex.get(list.getModel().getElementAt(i).toString()));
				orderedKeys.add((String) list.getModel().getElementAt(i));
			}

			// splitIndex=temp;
			updateChart();

		}

	}

}
/**
 * TransferHandler for list to drag categories in the list and rearrange the
 * boxplot
 * 
 * @author mrbai
 *
 */
class ListItemTransferHandler extends TransferHandler {
	protected final DataFlavor localObjectFlavor;
	protected int[] indices;
	protected int addIndex = -1; // Location where items were added
	protected int addCount; // Number of items added.

	public ListItemTransferHandler() {
		super();
		// localObjectFlavor = new ActivationDataFlavor(
		// Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
		localObjectFlavor = new DataFlavor(Object[].class, "Array of items");
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		JList<?> source = (JList<?>) c;
		c.getRootPane().getGlassPane().setVisible(true);

		indices = source.getSelectedIndices();
		Object[] transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
		// return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { localObjectFlavor };
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return Objects.equals(localObjectFlavor, flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (isDataFlavorSupported(flavor)) {
					return transferedObjects;
				} else {
					throw new UnsupportedFlavorException(flavor);
				}
			}
		};
	}

	@Override
	public boolean canImport(TransferSupport info) {
		return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
	}

	@Override
	public int getSourceActions(JComponent c) {
		Component glassPane = c.getRootPane().getGlassPane();
		glassPane.setCursor(DragSource.DefaultMoveDrop);
		return MOVE; // COPY_OR_MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport info) {
		TransferHandler.DropLocation tdl = info.getDropLocation();
		if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
			return false;
		}

		JList.DropLocation dl = (JList.DropLocation) tdl;
		JList target = (JList) info.getComponent();
		DefaultListModel listModel = (DefaultListModel) target.getModel();
		int max = listModel.getSize();
		int index = dl.getIndex();
		index = index < 0 ? max : index; // If it is out of range, it is appended to the end
		index = Math.min(index, max);

		addIndex = index;

		try {
			Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
			for (int i = 0; i < values.length; i++) {
				int idx = index++;
				listModel.add(idx, values[i]);
				target.addSelectionInterval(idx, idx);
			}
			addCount = values.length;
			return true;
		} catch (UnsupportedFlavorException | IOException ex) {
			ex.printStackTrace();
		}

		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		c.getRootPane().getGlassPane().setVisible(false);
		cleanup(c, action == MOVE);
	}

	private void cleanup(JComponent c, boolean remove) {
		if (remove && Objects.nonNull(indices)) {
			if (addCount > 0) {
				// https://github.com/aterai/java-swing-tips/blob/master/DragSelectDropReordering/src/java/example/MainPanel.java
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] >= addIndex) {
						indices[i] += addCount;
					}
				}
			}
			JList source = (JList) c;
			DefaultListModel model = (DefaultListModel) source.getModel();
			for (int i = indices.length - 1; i >= 0; i--) {
				model.remove(indices[i]);
			}
		}

		indices = null;
		addCount = 0;
		addIndex = -1;
	}

}