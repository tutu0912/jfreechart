package com.test;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.lowagie.text.DocumentException;

public class TestImg {
	@Test
	public void main() throws IOException, DocumentException {
		System.out.println("float格式化："+String.format("%1.3f", 66.5));
		System.exit(0);
		String imgFile = "F:\\test\\test.jpg";
		ExportPdfTemplate template = new ExportPdfTemplate();
		template.createExportFile(imgFile);
		//图标属性
		String chartTitle = "";
		String xTitle = "";
		String yTitle = "";
		String[] dataKeys = {"数值"};
		String[] columnKeys = {"11-01","11-02","11-03","11-04","11-05",
				"11-06","11-07","11-08","11-09","11-10","11-11",
				"11-12","11-13","11-14","11-15","11-16","11-17"
				,"11-18","11-19","11-20"};
		double[][] data  = new double[1][20];
		for(int i=0;i<20;i++) {
			while(true) {
				data[0][i] = new Random().nextInt(100000);
				if(data[0][i] > 10000) break;
			}
		}
		template.addChar(chartTitle, xTitle, yTitle, columnKeys, dataKeys, data, false);
		
		template.saveImg();
		
	}
	
}
