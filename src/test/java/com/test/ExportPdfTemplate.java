package com.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.PublicCloneable;
import org.jfree.util.StringUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AsianFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class ExportPdfTemplate {
	private static Font FONT = new Font("Arial", Font.CENTER_BASELINE, 10);
	private String fileStr;
	private List<JFreeChart> imgList = new ArrayList<JFreeChart>();
	//生成图片宽高
	private int imgWidth = 700;
	private int imgHeight =  400;
	//数据线颜色
	public static Color[] CHART_COLORS = {new Color(31, 129, 188),
            new Color(241, 92, 128), new Color(124, 181, 236),
            new Color(102, 172, 204), new Color(102, 102, 0),
            new Color(204, 153, 102), new Color(0, 153, 255),
            new Color(204, 255, 255), new Color(51, 153, 153),
            new Color(255, 204, 102), new Color(102, 102, 0),
            new Color(204, 204, 204), new Color(204, 255, 255),
            new Color(255, 204, 204), new Color(255, 255, 204),
            new Color(255, 153, 204), new Color(51, 0, 0),
            new Color(0, 51, 102), new Color(0, 153, 102),
            new Color(153, 102, 153), new Color(102, 153, 204),
            new Color(153, 204, 153), new Color(204, 204, 153),
            new Color(255, 255, 153), new Color(255, 204, 153),
            new Color(255, 153, 204), new Color(204, 153, 153),
            new Color(204, 204, 255), new Color(204, 255, 204),
            new Color(255, 204, 102),new Color(153, 204, 153)};// 颜色

	private int dataLastPointer = -1;
	
	/**
	 * 创建PDF文件
	 * @param _fileStr
	 * @throws IOException
	 */
	public void createExportFile(String _fileStr) throws IOException {
		this.fileStr = _fileStr;
//		if(!checkFileValid()) throw new IOException("文件格式不合法，创建文件失败！");
		File file = new File(this.fileStr);
		FileUtils.touch(file);
		setChartTheme();
	}
	/**
	 * 向文件中添加图表
	 * @param chartTitle
	 * @param xTitle
	 * @param yTitle
	 * @param columnKeys
	 * @param dataKeys
	 * @param data
	 */
	public void addChar(String chartTitle, String xTitle, String yTitle,
			String[] columnKeys,String[] dataKeys,double[][] data,boolean isInteger) {
		if(data != null && data.length >= 1) this.dataLastPointer = data[0].length - 1;
		CategoryDataset dataset = this.getBarData(dataKeys, columnKeys, data);
		JFreeChart chart = this.setChartData(chartTitle,xTitle,yTitle,dataset,20000,isInteger);
		chart.setID("");
        imgList.add(chart);
	}
	/**
	 * @throws IOException 
	 * 
	 */
	public void saveImg() throws IOException {
		if(fileStr == null) throw new IOException("保存时未找到文件："+fileStr);
		OutputStream out = new FileOutputStream(fileStr);
		for(JFreeChart chart : imgList){
			ChartUtilities.writeChartAsJPEG(out,1.0f,chart,500,350,null);
		}
		out.close();
	}
	
	/**
	 * 保存pdf文件
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public void savePdfFile() throws IOException, DocumentException {
		if(fileStr == null) throw new IOException("保存时未找到文件："+fileStr);
		OutputStream out = new FileOutputStream(fileStr);
		int width = imgWidth;
		int height = imgHeight;
		Document doc = null;
		PdfWriter writer = null;
		FontMapper mapper = new AsianFontMapper("STSong-Light","UniGB-UCS2-H");
		
		mapper.awtToPdf(FONT);
		PdfContentByte cb = null;
		PdfTemplate tp = null;
		int i=1;
		for(JFreeChart chart : imgList){
			if(doc == null) {
				doc = new Document(new Rectangle(imgWidth, imgHeight * imgList.size()), 0, 50, 0, 50);
				writer = PdfWriter.getInstance(doc, out);
				doc.open();
				cb = writer.getDirectContent();
				tp = cb.createTemplate(width, imgHeight * imgList.size());
			}
//        	doc.setPageSize(new Rectangle(img.getWidth(), img
//                    .getHeight()));
//        	doc.open();
//        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ImageIO.write(img, "png", baos);
//        	doc.add(Image.getInstance(baos.toByteArray()));
//        	baos.close();
			doc.open();
            Graphics2D g2 = tp.createGraphics(width, i*height - 5, mapper);
            Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);  
            chart.draw(g2, r2D);  
            g2.dispose();
            i++;
//            Table table = new Table(2);
//            Cell c1 = new Cell();
//            c1.add(tp);
        }
        cb.addTemplate(tp,0,0);
    	doc.close();
		out.close();
	}
	
	/**
	 * 生成图表数据集
	 * @param dataKeys
	 * @param columnKeys
	 * @param data
	 * @return
	 */
	private CategoryDataset getBarData(String[] dataKeys,
            String[] columnKeys,double[][] data) {
        return DatasetUtilities.createCategoryDataset(dataKeys, columnKeys, data);
    }
	/**
	 * 填充图表数据
	 * @param chartTitle
	 * @param xTitle
	 * @param yTitle
	 * @param xyDataset
	 * @return
	 */
	private JFreeChart setChartData(String chartTitle, String xTitle, String yTitle,
            CategoryDataset xyDataset,double tickUnit,boolean isInteger) {
		JFreeChart chart = ChartFactory.createLineChart(chartTitle, xTitle, yTitle,
                xyDataset, PlotOrientation.VERTICAL, false, true, false);
		chart.setPadding(new RectangleInsets(20D, 70D, 20D, 70D));
		chart.setTextAntiAlias(true);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderPaint(Color.GRAY);
        chart.setBorderVisible(true);
        // 设置图标题的字体重新设置title
        Font font = new Font("宋体", Font.PLAIN, 14);
        TextTitle title = new TextTitle(chartTitle);
        title.setMargin(new RectangleInsets(20D, 00D, 0D, 00D));
        title.setFont(font);
        title.setPosition(RectangleEdge.RIGHT);
        chart.setTitle(title);
        
        // 设置面板字体
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot categoryplot = (CategoryPlot) chart.getPlot();
        categoryplot.setDomainGridlinesVisible(false); //x轴 网格是否可见
        categoryplot.setRangeGridlinesVisible(false); //y轴 网格是否可见
//        categoryplot.setRangeGridlinePaint(Color.WHITE); //x轴 虚线色彩
//        categoryplot.setDomainGridlinePaint(Color.WHITE); //y轴 虚线色彩
//        categoryplot.setBackgroundPaint(Color.WHITE);
        categoryplot.setAxisOffset(new RectangleInsets(0D, 0D, 0D, 0D));// 设置轴和面板之间的距离
        //X轴样式
        CategoryAxis domainAxis = categoryplot.getDomainAxis();
        domainAxis.setLabelFont(labelFont);// x轴标题
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 14));// x轴数值
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90); //斜体90度
        domainAxis.setLowerMargin(0); // 设置距离图片左端距离
        domainAxis.setUpperMargin(0.05D); // 设置距离图片右端距离
        //Y轴样式
        NumberAxis numberaxis = (NumberAxis) categoryplot.getRangeAxis();
        numberaxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 14));// y轴数值
        numberaxis.setLabelFont(labelFont); // y轴标题
        /**
         * 计算刻度，是刻度保持在6-9个之间，并且下部保持两个空白刻度
         */
        double lower = xyDataset.getValue(0, 0).floatValue();
        double upper = xyDataset.getValue(0, 0).floatValue();
        for(int i=0;i<xyDataset.getColumnCount();i++) {
        	float number = xyDataset.getValue(0, i).floatValue();
        	if(number > upper) {
        		upper = number;
        	}
        	if(number < lower) {
        		lower = number;
        	}
        }
        double domain = upper - lower;
        double tmpTick = domain / 6;
        if(tmpTick > 1000) {
        	int hight = Integer.parseInt(String.valueOf(tmpTick).substring(0, 1));
        	int mid = Integer.parseInt(String.valueOf(tmpTick).substring(1, 2));
        	if(mid > 5) {
        		hight++;
        	}else {
        		mid = 5;
        	}
        	int len = String.valueOf((int)tmpTick).length() - 2;
        	tmpTick = Integer.parseInt(new StringBuilder().append(hight).append(mid).append(String.format("%0"+len+"d", 0)).toString());
        }else if(tmpTick > 100) {
        	int hight = Integer.parseInt(String.valueOf(tmpTick).substring(0, 1));
        	int mid = Integer.parseInt(String.valueOf(tmpTick).substring(1, 2));
        	if(mid > 5) {
        		hight++;
        	}else {
        		mid = 5;
        	}
        	tmpTick = Integer.parseInt(new StringBuilder().append(hight).append(mid).append("0").toString());
        }else if(tmpTick > 10) {
        	if(tmpTick > 50) {
        		tmpTick = 100;
        	}else{
        		tmpTick = 50;
        	}
        }else if(tmpTick > 1) {
        	tmpTick = (int)tmpTick;
        }else if(tmpTick > 0) {
        	tmpTick = 0.05;
        }
        System.out.println("tmpTick:"+tmpTick);
        upper += tmpTick;
        if(lower - 2*tmpTick > 0) {
        	lower -= 2*tmpTick;
        	System.out.println("减:"+(2*tmpTick));
        }else if(lower - tmpTick > 0) {
        	lower -= tmpTick;
        	System.out.println("减:"+tmpTick);
        }else{
        	lower = 0;
        }
        numberaxis.setRange(new Range(lower,upper));
        NumberTickUnit ntu= new NumberTickUnit(tmpTick);
        numberaxis.setTickUnit(ntu);
        if(isInteger) {
        	numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        } else {
        	numberaxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        }
        numberaxis.setAutoRangeIncludesZero(false);
        numberaxis.setAxisLineVisible(false);
        numberaxis.setTickMarksVisible(false);
        
        numberaxis.setUpperMargin(0.2D);
//        numberaxis.set
        
        // 获得renderer
        LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer) categoryplot
                .getRenderer();
        lineandshaperenderer.setBaseShapesVisible(true); // series 点（即数据点）可见
        lineandshaperenderer.setBaseLegendTextFont(labelFont);
        lineandshaperenderer.setSeriesStroke(0, new BasicStroke(2.0F));
        lineandshaperenderer.setSeriesPaint(0, Color.BLUE);
        lineandshaperenderer.setSeriesShapesVisible(0,true);
        lineandshaperenderer.setSeriesShape(0,new Ellipse2D.Double(-3,-3,6,6));
        //显示折点数据
        OnlyLastPointerShowCategoryItemLabelGenerator labelGenerator = 
        		new OnlyLastPointerShowCategoryItemLabelGenerator();
        labelGenerator.pointer = this.dataLastPointer;
        lineandshaperenderer.setBaseItemLabelFont(new Font("Arial", Font.BOLD, 14));
        lineandshaperenderer.setBaseItemLabelGenerator(labelGenerator);
        //显示折点
        lineandshaperenderer.setBaseItemLabelsVisible(true);
        
        
        chart.getRenderingHints().
        	put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		return chart;
	}
	
	/**
	 * 检查文件名是否合法
	 * @return
	 */
	private boolean checkFileValid() {
		if(StringUtils.endsWithIgnoreCase(this.fileStr, ".pdf")) 
			return true;
		else 
			return false;
	}
	
	public void setChartTheme() {
        // 设置中文主题样式 解决乱码
        StandardChartTheme chartTheme = new StandardChartTheme("CN");
        // 设置标题字体
        chartTheme.setExtraLargeFont(FONT);
        // 设置图例的字体
        chartTheme.setRegularFont(FONT);
        // 设置轴向的字体
        chartTheme.setLargeFont(FONT);
        chartTheme.setSmallFont(FONT);
        chartTheme.setTitlePaint(new Color(51, 51, 51));
        chartTheme.setSubtitlePaint(new Color(85, 85, 85));
 
        chartTheme.setLegendBackgroundPaint(Color.WHITE);// 设置标注
        chartTheme.setLegendItemPaint(Color.BLACK);//
        chartTheme.setChartBackgroundPaint(Color.WHITE);
        // 绘制颜色绘制颜色.轮廓供应商
        // paintSequence,outlinePaintSequence,strokeSequence,outlineStrokeSequence,shapeSequence
 
        Paint[] OUTLINE_PAINT_SEQUENCE = new Paint[]{Color.WHITE};
        // 绘制器颜色源
        DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(
                CHART_COLORS, CHART_COLORS, OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
        chartTheme.setDrawingSupplier(drawingSupplier);
 
        chartTheme.setPlotBackgroundPaint(Color.WHITE);// 绘制区域
        chartTheme.setPlotOutlinePaint(Color.WHITE);// 绘制区域外边框
        chartTheme.setLabelLinkPaint(new Color(8, 55, 114));// 链接标签颜色
        chartTheme.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);
 
        chartTheme.setAxisOffset(new RectangleInsets(5, 12, 5, 12));
        chartTheme.setDomainGridlinePaint(new Color(192, 208, 224));// X坐标轴垂直网格颜色
        chartTheme.setRangeGridlinePaint(new Color(192, 192, 192));// Y坐标轴水平网格颜色
 
        chartTheme.setBaselinePaint(Color.WHITE);
        chartTheme.setCrosshairPaint(Color.BLUE);// 不确定含义
        chartTheme.setAxisLabelPaint(new Color(51, 51, 51));// 坐标轴标题文字颜色
        chartTheme.setTickLabelPaint(new Color(67, 67, 72));// 刻度数字
        chartTheme.setBarPainter(new StandardBarPainter());// 设置柱状图渲染
        chartTheme.setXYBarPainter(new StandardXYBarPainter());// XYBar 渲染
 
        chartTheme.setItemLabelPaint(Color.black);
        chartTheme.setThermometerPaint(Color.white);// 温度计
 
        ChartFactory.setChartTheme(chartTheme);
    }
	
	class OnlyLastPointerShowCategoryItemLabelGenerator 
			extends AbstractCategoryItemLabelGenerator 
			implements CategoryItemLabelGenerator, Cloneable, PublicCloneable, Serializable {

		private static final long serialVersionUID = 3499701401211412882L;
	    public static final String DEFAULT_LABEL_FORMAT_STRING = "{2}";
	    public int pointer = -1;

	    public OnlyLastPointerShowCategoryItemLabelGenerator() {
	        super("{2}", NumberFormat.getInstance());
	    }

	    public OnlyLastPointerShowCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter) {
	        super(labelFormat, formatter);
	    }

	    public OnlyLastPointerShowCategoryItemLabelGenerator(String labelFormat, NumberFormat formatter, NumberFormat percentFormatter) {
	        super(labelFormat, formatter, percentFormatter);
	    }

	    public OnlyLastPointerShowCategoryItemLabelGenerator(String labelFormat, DateFormat formatter) {
	        super(labelFormat, formatter);
	    }

	    public String generateLabel(CategoryDataset dataset, int row, int column) {
	    	String res = "";
	    	if(column == this.pointer && this.pointer != -1) {
	    		res = this.generateLabelString(dataset, row, column);
	    	}
	        return res.replaceAll(",", "");
	    }

	    public boolean equals(Object obj) {
	        if (obj == this) {
	            return true;
	        } else {
	            return !(obj instanceof OnlyLastPointerShowCategoryItemLabelGenerator) ? false : super.equals(obj);
	        }
	    }
		
	}

}
