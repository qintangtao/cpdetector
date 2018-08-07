package com.qin.cp;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class CharsetCmd {

	private static final String TAG = CharsetCmd.class.getSimpleName();

	String[] mArgs;
	int mNextArg;

	public void doMain(String[] args) {
		mArgs = args;
		mNextArg = 0;

		String sourceFilePath = "";
		String targetFilePath = "";
		String sourceCharset = "";
		String targetCharset = "";

		while (true) {
			String arg = nextArg();
			if (arg == null) {
				break;
			}

			if ("-file".equals(arg)) {
				arg = nextArg();
				if (arg == null) {
					break;
				}
				sourceFilePath = arg;
				MyLog.d(TAG, "-file " + arg);
			} else if ("-save".equals(arg)) {
				arg = nextArg();
				if (arg == null) {
					break;
				}
				targetFilePath = arg;
				MyLog.d(TAG, "-save " + arg);
			} else if ("-charset".equals(arg)) {
				arg = nextArg();
				if (arg == null) {
					break;
				}
				targetCharset = arg;
				MyLog.d(TAG, "-charset " + arg);
			} else if ("-help".equals(arg)) {
				help();
				return;
			}
		}
		
		if (sourceFilePath.isEmpty()) {
			help();
			return;
		}
		
		if (!fileIsExists(sourceFilePath)) {
			System.out.println("no such file " + sourceFilePath);
			return;
		}

		sourceCharset = getFileEncode(sourceFilePath);
		if (sourceCharset == null) {
			System.out.println("unknown charset");
			return;
		}

		System.out.println(sourceCharset);
		
		if (targetFilePath.isEmpty() || targetCharset.isEmpty()) {
			return;
		}
		
		if (sourceCharset.toUpperCase().equals(targetCharset.toUpperCase())) {
			return;
		}

		convert(sourceFilePath, targetFilePath, sourceCharset, targetCharset);
	}

	String nextArg() {
		if (mNextArg >= mArgs.length) {
			return null;
		}
		String arg = mArgs[mNextArg];
		mNextArg++;
		return arg;
	}

	void help() {
		System.out.println("cpdetector -file file path -save convert file path -charset convert file charset");
	}
	
	void convert(String sourceFilePath, String targetFilePath, String sourceCharset, String targetCharset) {

		try {
			File file = new File(sourceFilePath);

			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					file), sourceCharset);
			BufferedReader br = new BufferedReader(isr);
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				// line = URLEncoder.encode(line, "utf8");
				sb.append(line + "\r\n");
			}
			br.close();
			isr.close();

			File targetFile = new File(targetFilePath);
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(targetFile), targetCharset);
			BufferedWriter bw = new BufferedWriter(osw);
			// bw.write(URLDecoder.decode(sb.toString(), "utf8"));
			bw.write(sb.toString());
			bw.close();
			osw.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	String changeCharset(String str, String oldCharset, String newCharset) {
		if (str != null) {
			try {
				byte[] bs = str.getBytes(oldCharset);
				return new String(bs, newCharset);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return null;
	}

	String getFileEncode(String filePath) {

		String charsetName = null;

		try {
			File file = new File(filePath);
			CodepageDetectorProxy detector = CodepageDetectorProxy
					.getInstance();
			detector.add(new ParsingDetector(false));
			detector.add(JChardetFacade.getInstance());
			detector.add(ASCIIDetector.getInstance());
			detector.add(UnicodeDetector.getInstance());
			Charset charset = detector.detectCodepage(file.toURI().toURL());
			if (charset != null) {
				charsetName = charset.name();
			} else {
				charsetName = "UTF-8";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return charsetName;
	}

	boolean fileIsExists(String strFile) {
		try {
			File f = new File(strFile);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
