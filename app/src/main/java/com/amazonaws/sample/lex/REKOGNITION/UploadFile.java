package com.amazonaws.sample.lex.REKOGNITION;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadFile extends AsyncTask<String, String, String> {
    @Override
    protected String doInBackground(String[] params) {

        rekogObject ret = new rekogObject();
        String pollyMessage = new String();
        try {
            Uri uri = Uri.parse(params[0]);
            int type = Integer.parseInt(params[1]);
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024 * 5;
            InputStream is = null;
            ByteArrayOutputStream baos = null;

            //todo change URL as per client ( MOST IMPORTANT )
            URL url = null;

            if(type == 0) // 사물인식
            {
                //Log.d("hello","is it okay??");
                url = new URL("https://2v89sh5096.execute-api.us-east-1.amazonaws.com/dev/fileupload");
            }else if(type == 1) // 지페인식
            {
                url = new URL("https://jt881z1682.execute-api.us-east-1.amazonaws.com/dev/fileupload");
            }
            //Log.d("hello", url.toString());
            if(url != null){
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Allow Inputs &amp; Outputs.
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // Set HTTP method to POST.
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                FileInputStream fileInputStream;
                DataOutputStream outputStream;
                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);

                outputStream.writeBytes("Content-Disposition: form-data; name=\"Key\"; filename=\"AeyehelperTest.jpg\"" + lineEnd);
                outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
                outputStream.writeBytes(lineEnd);
                //outputStream.writeBytes(twoHyphens + boundary + lineEnd);

                //outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadFile\";filename=\"AeyeTest" + "hello" +"\"" + lineEnd);
                //outputStream.writeBytes(lineEnd);

                fileInputStream = new FileInputStream(uri.getPath());
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file


                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                Log.d("hello bytesRead : ", String.valueOf(bytesRead));

                while (bytesRead > 0) {

                    DataOutputStream dataWrite = new DataOutputStream(connection.getOutputStream());
                    dataWrite.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                fileInputStream.close();
                outputStream.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd);
                outputStream.flush();
                // Responses from the server (code and message)
                int serverResponseCode = connection.getResponseCode();

                Log.d("hello",serverResponseCode + "");
                if (serverResponseCode == 200) {
                    is = connection.getInputStream();
                    if(type == 0){
                        objectRekogParser rP = new objectRekogParser();
                        ret = rP.readJsonStream(is);
                        Log.d("hello", ret.getName());
                        if(ret.getName() != "fail to detect"){
                            pollyMessage = "가장 높은 결과값은 " + ret.getName() + "이며 정확도는 " + ret.getConfidence() + "입니다.";
                            if(ret.getWords()[0].equals("xxxxx")){

                                pollyMessage += "탐지된 글자는 없습니다.";
                            }else{
                                pollyMessage += "탐지된 글자는";
                                for(int i = 0;i< ret.getWords().length;i++){
                                    pollyMessage = pollyMessage + " " +  ret.getWords()[i];
                                }
                                pollyMessage += " 입니다.";
                            }
                        }else
                        {
                            pollyMessage = "탐지된 물체가 없습니다.";
                        }

                    }else if(type == 1){
                        objectRekogParser rP = new objectRekogParser();
                        ret = rP.readJsonStream(is);

                        if(ret.getName().equals("is Money")){
                            pollyMessage = ret.getWords()[0] + "원 지폐로 확인 되었습니다.";
                        }else
                            pollyMessage = "지폐가 아닙니다.";
                    }

                }
                outputStream.close();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
        return pollyMessage;
    }


}