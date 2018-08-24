package com.amazonaws.sample.lex.REKOGNITION;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class objectRekogParser {

    public rekogObject readJsonStream(InputStream in) throws IOException{

        rekogObject ret = new rekogObject();

        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return responseParse(reader);
        } finally {
            reader.close();
        }
    }
    public rekogObject responseParse(JsonReader reader) throws  IOException{
        rekogObject ret = new rekogObject();


        List<String> words = new ArrayList<String>();
        int degree = 0;

        reader.beginObject();
        objectValue temp = new objectValue();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("Name")) {
                ret.setName(reader.nextString());
            } else if (name.equals("Confidence")) {
                ret.setConfidence(reader.nextDouble());
            }else if (name.equals("detectWord")){
                words = readStringArray(reader);
                if(words != null)
                {
                    ret.setWords(words.toArray(new String[words.size()]));
                }
            }
        }
        reader.endObject();
        return ret;
    }

    public List<String> readStringArray(JsonReader reader) throws IOException {
        List<String> words = new ArrayList<String>();

        reader.beginArray();
        while (reader.hasNext()) {
            String nextWord = reader.nextString();
            words.add(nextWord);
        }
        reader.endArray();
        return words;
    }
}
