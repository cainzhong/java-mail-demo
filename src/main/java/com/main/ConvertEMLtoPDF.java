// package com.main;
//
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
//
// import org.w3c.tidy.Tidy;
// import org.xhtmlrenderer.pdf.ITextRenderer;
//
// import com.aspose.email.MailMessage;
// import com.aspose.email.MhtFormatOptions;
// import com.aspose.email.MhtSaveOptions;
// import com.aspose.email.SaveOptions;
// import com.lowagie.text.DocumentException;
//
// public class ConvertEMLtoPDF {
// public static void cleanHtml(String filename) {
// File file = new File(filename + ".html");
// InputStream in = null;
// try {
// in = new FileInputStream(file);
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// }
// OutputStream out = null;
// try {
// out = new FileOutputStream(filename + ".xhtml");
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// }
// final Tidy tidy = new Tidy();
// tidy.setQuiet(false);
// tidy.setShowWarnings(true);
// tidy.setShowErrors(0);
// tidy.setMakeClean(true);
// tidy.setForceOutput(true);
// org.w3c.dom.Document document = tidy.parseDOM(in, out);
// }
//
// public static void createPdf(String filename) throws IOException, DocumentException {
// OutputStream os = new FileOutputStream(filename + ".pdf");
// ITextRenderer renderer = new ITextRenderer();
// renderer.setDocument(new File(filename + ".xhtml"));
// renderer.layout();
// renderer.createPDF(os);
// os.close();
// }
//
// public static void main(String args[]) throws IOException, DocumentException {
// String fileName = "C:/Users/zhontao/AppData/Local/Temp/temp/201ef8d6-adbf-4282-8ac2-d8b16a5ccd9a.eml";
// String outFileName = "C:/Users/zhontao/AppData/Local/Temp/aspose-email.html";
// String mhtfileName = "C:/Users/zhontao/AppData/Local/Temp/aspose-email.mht";
//
// MailMessage eml = MailMessage.load(fileName);
// // HtmlSaveOptions options = SaveOptions.getDefaultHtml();
// // options.setEmbedResources(true);
// // options.setHtmlFormatOptions(HtmlFormatOptions.WriteHeader | HtmlFormatOptions.WriteCompleteEmailAddress | WriteOutlineAttachments);
// // eml.save(outFileName, options);
// MhtSaveOptions opt = SaveOptions.getDefaultMhtml();
// opt.setMhtFormatOptions(opt.getMhtFormatOptions() | MhtFormatOptions.WriteOutlineAttachments);
// eml.save(mhtfileName, opt);
// }
// }
