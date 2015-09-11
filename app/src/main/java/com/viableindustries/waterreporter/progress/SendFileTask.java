//package com.viableindustries.waterreporter.progress;
//
//import java.io.File;
//import java.util.logging.Logger;
//
//private class SendFileTask extends AsyncTask<String, Integer, ApiResult> {
//
//    private ProgressListener listener;
//
//    private String filePath;
//
//    private FileType fileType;
//
//    public SendFileTask(String filePath, FileType fileType) {
//
//        this.filePath = filePath;
//
//        this.fileType = fileType;
//
//    }
//
//    @Override
//    protected ApiResult doInBackground(String... params) {
//
//        File file = new File(filePath);
//
//        totalSize = file.length();
//
//        Logger.d("Upload FileSize[%d]", totalSize);
//
//        listener = new ProgressListener() {
//
//            @Override
//            public void transferred(long num) {
//
//                publishProgress((int) ((num / (float) totalSize) * 100));
//
//            }
//
//        };
//
//        String _fileType = FileType.VIDEO.equals(fileType) ? "video/mp4" : (FileType.IMAGE.equals(fileType) ? "image/jpeg" : "*/*");
//
//        return MyRestAdapter.getService().uploadFile(new CountingTypedFile(_fileType, file, listener), "/Mobile Uploads");
//    }
//
//    @Override
//    protected void onProgressUpdate(Integer... values) {
//        Logger.d(String.format("progress[%d]", values[0]));
//        //do something with values[0], its the percentage so you can easily do
//        //progressBar.setProgress(values[0]);
//    }
//
//}
