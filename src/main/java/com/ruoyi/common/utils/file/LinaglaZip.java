package com.ruoyi.common.utils.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.base.Strings;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * 
 * @author vito
 *
 */
public class LinaglaZip {
	
	public static void zip(String currentPath, String toFilePath, String password)throws Exception{
		zip(new File(currentPath),toFilePath,password);
	}
	
	public static void zip(File currentDir, String toFilePath, String password) throws Exception {
	    // 生成的压缩文件
	    ZipFile zipFile = new ZipFile(toFilePath);
	    zipFile.setCharset(Charset.forName("GBK"));
	    ZipParameters parameters = new ZipParameters();
	    // 压缩方式
	    parameters.setCompressionMethod(CompressionMethod.DEFLATE);
	    // 压缩级别
	    parameters.setCompressionLevel(CompressionLevel.NORMAL);
	    
	    // 设置密码
	    if(!Strings.isNullOrEmpty(password)) {
	    	// 是否设置加密文件
		    parameters.setEncryptFiles(true);
		    // 设置加密算法
		    parameters.setEncryptionMethod(EncryptionMethod.AES);
		    // 设置AES加密密钥的密钥强度
		    parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
		    
	       zipFile.setPassword(password.toCharArray());
	    }
	     if(new File(toFilePath).exists()){
	    	 new File(toFilePath).delete();
	     }
	     //压缩文件
	    if(currentDir.isFile()){
	    	zipFile.addFile(currentDir, parameters);
	     }else{
	    	// 要打包的文件夹
	 	    File[] fs = currentDir.listFiles();

	 	    // 遍历test文件夹下所有的文件、文件夹
	 	    for (File f : fs) {
	 	        if (f.isDirectory()) {
	 	            zipFile.addFolder(f, parameters);
	 	        } else {
	 	            zipFile.addFile(f, parameters);
	 	        }
	 	    }
	     }
	    
	}
	
	public static void unzip(String zipFilePath, String toPath, String password) throws Exception {
	    // 生成的压缩文件
	    ZipFile zipFile = new ZipFile(zipFilePath);
	    // 设置密码
	    if(!Strings.isNullOrEmpty(password)) {
	        zipFile.setPassword(password.toCharArray());
	    }
	    System.out.println(zipFile.getCharset());
//	    zipFile.setCharset(Charset.forName("GBK")); //2023.8.8改
//	    if(zipFile.getCharset().toString()!="UTF-8"){
//	    	zipFile.setCharset(Charset.forName("GBK"));	
//	    }
	    zipFile.setCharset(Charset.forName("UTF-8"));
	    try{
	    	//判断压缩包是否是UTF-8 
	    	if(!testEncoding(zipFilePath)){
	    		zipFile.setCharset(Charset.forName("GBK"));
	    	}
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    // 解压缩所有文件以及文件夹
	    zipFile.extractAll(toPath);
	}
	
	public static boolean testEncoding(String filepath) throws FileNotFoundException {
	    FileInputStream fis = new FileInputStream(new File(filepath));
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    ZipInputStream zis = new ZipInputStream(bis, Charset.forName("UTF-8"));
	    ZipEntry zn = null;
	    try {
	        while ((zn = zis.getNextEntry()) != null) {
	            // do nothing
	        }
	    } catch (Exception e) {
	        return false;
	    }finally {
	        try {
	            zis.close();
	            bis.close();
	            fis.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return true;
	}


	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		//zip(new File("/home/dagl/测试乱码问题"),"/home/dagl/测试乱码问题.zip","");
		
		//unzip("/home/dagl/A1-2021-10年-0004.zip","/home/dagl/","");
		
		//unzip("/home/dagl/测试乱码问题.zip","/home/dagl/","");

		System.out.println("压缩包格式UTF-8："+testEncoding("C:/Users/xiaozhi/Desktop/盐田接收包/1.zip"));
	}

}
