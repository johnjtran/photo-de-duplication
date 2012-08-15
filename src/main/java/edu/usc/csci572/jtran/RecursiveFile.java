package edu.usc.csci572.jtran;

import java.io.File;
import java.util.ArrayList;

public class RecursiveFile {

	private ArrayList<String> files = new ArrayList<String>();
	ArrayList<String> watchPatterns = new ArrayList<String>();

	RecursiveFile(String path) {
		walk(path);
	}

	RecursiveFile(String path, String pattern) {
		watchPatterns.add(pattern);
		walk(path);
	}

	public RecursiveFile(String path, ArrayList<String> skipPatterns) {
		this.watchPatterns.addAll(skipPatterns);
		walk(path);
	}

	private void walk(String path) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null) {
			return;
		}
		
		for (File f : list) {
			if (f.isDirectory()) {
				walk(f.getAbsolutePath());
			} else {
				String filename = f.getAbsolutePath();
				if (keep(filename))
					getFiles().add(filename);
			}
		}
	}

	private boolean keep(String s) {
		for (String pattern : watchPatterns) {
			if (s.endsWith(pattern))
				return true;
		}

		return false;
	}

	public void print() {
		for (String filename : getFiles()) {
			System.out.println("[" + filename + "]");
		}
	}

	public static void main(String[] args) {
		RecursiveFile fw = new RecursiveFile(args[0]);
		fw.print();
	}

	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}

	public ArrayList<String> getFiles() {
		return files;
	}
}
