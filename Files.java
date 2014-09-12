package com.test.fullscreen;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;


public class Files {
	/** 
	 * Get file content as bytes array. 
	 * @return byte array on success, NULL on error. 
	 * */
	public static byte[] getFileBytes(String path) {
		byte[] content = null;
		
		File f = new File(path);
		
		if ((f.exists()) && f.isFile() && f.canRead()) {
			InputStream inputStream = null;
			try {
				int length = (int) f.length();
				inputStream = new FileInputStream(path);
				byte[] buffer = new byte[length];
				int d = inputStream.read(buffer);
				
				content = buffer;
			} catch(Exception e) {
				Log.e("getFileBytes", e.getMessage() + "");
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {}
			}
		}
		
		return content;
	}
	
	/** 
	 * Saving bytes array as file. 
	 * File will be rewrited or created if it does not exist. 
	 * @return true on success, false on error. 
	 * */
	public static boolean saveFileBytes(byte[] bytes, String path) {
		boolean res = false;
		
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(path);
			outStream.write(bytes);
			outStream.flush();
			
			if ((new File(path)).isFile()) {
				res = true;
			}
		} catch(Exception e) {
			Log.e("saveFileBytes", e.getMessage() + "");
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {}
		}
		
		return res;
	}
	
	
	@SuppressLint("NewApi")
	public static File getExternalCard() {
		File extCard = new File("/mnt/");
		if (Environment.isExternalStorageRemovable()) {
			extCard = Environment.getExternalStorageDirectory().getParentFile();
		}
		try {
			extCard = new File("/mnt/external_sd/");
			if (extCard.isDirectory()) {
				return extCard;
			}
		} catch(Exception e) { }
		try {
			extCard = new File("/mnt/extSdCard/");
			if (extCard.isDirectory()) {
				return extCard;
			}
		} catch(Exception e) { }
		
		return extCard;
	}
	
	public static String pathFromUri(Uri uri, Context context) {
		String path = null;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, null, null, null, null);
			cursor.moveToFirst();
			String document_id = cursor.getString(0);
			document_id = document_id.substring(document_id.lastIndexOf(":")+1);
			cursor.close();
			
			cursor = context.getContentResolver().query( 
			android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
			cursor.moveToFirst();
			path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		} catch(Exception e) {
			path = uri.toString();
		} finally {
			cursor.close();
		}
		
		return path;
	}
	public static Uri UriFromPath(String path) {
		Uri uri;
		uri = Uri.fromFile(new File(path));
		
		return uri;
	}
	
	@SuppressLint("DefaultLocale")
	public static String getExtension(String path) {
		String res = null;
		
		String extension = MimeTypeMap.getFileExtensionFromUrl(path);
		if (extension != null) {
			return extension.toLowerCase();
		}
		else {
			try {
				String[] parts = path.split("\\.");
				int len = parts.length;
				res = (parts[len - 1]).toLowerCase();
			} catch(Exception e) { Log.e("getExtension", e.getMessage() + ""); }
		}
		
		return res;
	}
	
	
	/*
	 * Images 
	 */
	public static boolean saveBitmap(byte[] data, String path, int quality) {
		boolean res = false;
		
		if ((quality > 100) || (quality <= 0)) {
			quality = 100;
		}
		
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path);
			bmp.compress(Bitmap.CompressFormat.PNG, quality, out);
			out.flush();
			
			res = true;
		} catch (Exception e) {
			Log.e("saveBitmap", e.getMessage() + "");
		} finally {
			try {
				out.close();
			} catch (IOException e) {}
		}
		
		return res;
	}
	
	public static boolean isImage(String path) {
		boolean res = false;
		File f = new File(path);
		if (f.isFile()) {
			try {
				String ext = Files.getExtension(path);
				if (ext.equals("jpg") || 
						ext.equals("jpeg") || 
						ext.equals("gif") || 
						ext.equals("bmp") || 
						ext.equals("png")) {
					res = true;
				}
			} catch(Exception e) { Log.e("isImage", e.getMessage() + ""); }
		}
		
		return res;
	}
	
	/** 
	 * Compress image. 
	 * @return bitmap for compressed image. 
	 * */
	public static Bitmap getPreviewBitmap(String path, int maxWidth, int maxHeight) {
		Bitmap bitm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeFile(path, options);
			int sc = sampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight);
			options.inJustDecodeBounds = false;
			options.inSampleSize = sc;
			bitm = BitmapFactory.decodeFile(path, options);
		} catch(Exception e) {
			Log.e("getPreviewBitmap", e.getMessage() + "");
			e.printStackTrace();
		}
		
		return bitm;
	}
	
	//Calculating the required degree of image compression: 
	private static int sampleSize(int width, int height, int maxWidth, int maxHeight) {
		int sample = 1;
		if ((width > maxWidth) && (height > maxHeight) && 
				(maxWidth > 0) && (maxHeight > 0)) {
			float w = (float) width / maxWidth;
			float h = (float) height / maxHeight;
			sample = (int) Math.ceil((w + h) / 2);
		}
		
		return sample;
	}
}


