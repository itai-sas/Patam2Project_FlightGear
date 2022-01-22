package model;
import other.StatLib;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TimeSeries
{

    private ArrayList<String> features;
    private Map<String, ArrayList<Float>> tsMap;
    private Map<String, Correlation> corMap;

    private int dataRowSize;

    public TimeSeries(String csvFileName)
    {
        features = new ArrayList<>();
        tsMap = new HashMap<>();


        try {
            BufferedReader in=new BufferedReader(new FileReader(csvFileName));
            String line=in.readLine();
            for(String att : line.split(","))
            {
                features.add(att);
                tsMap.put(att, new ArrayList<>());
            }
            while((line=in.readLine())!=null)
            {
                int i=0;
                for(String val : line.split(","))
                {
                    tsMap.get(features.get(i)).add(Float.parseFloat(val));
                    i++;
                }
            }
            dataRowSize=tsMap.get(features.get(0)).size();
            this.corCalc();

            in.close();
        }catch(IOException e) {}
    }




    public ArrayList<Float> getFeatureData(String name)
    {
        return tsMap.get(name);
    }

    public ArrayList<String> getFeatures()
    {
        return features;
    }

    public int getRowSize()
    {
        return dataRowSize;
    }

    public ArrayList<Float> getRow(int index
    ){
        ArrayList<Float> row = new ArrayList<>();
        for(String f : features){
            row.add(tsMap.get(f).get(index));
        }
        return row;
    }

    public Map<String, Correlation> getCorMap()
    {
        if (this.corMap==null)
            this.corCalc();
        return corMap;
    }

    private void corCalc ()
    {
        corMap=new HashMap<>();
        for(String feature1 : features)
        {
            String maxCorlFeature = "";
            float maxCorl = 0;
            for (String feature2 : features)
            {
                if (!feature1.equals(feature2))
                {
                    ArrayList<Float> f1 = this.getFeatureData(feature1);
                    ArrayList<Float> f2 = this.getFeatureData(feature2);

                    float[] f1Arr = new float[f1.size()];
                    float[] f2Arr = new float[f2.size()];
                    for (int i = 0; i < f1.size(); i++)
                    {
                        f1Arr[i] = f1.get(i);
                        f2Arr[i] = f2.get(i);
                    }
                    float correlation = StatLib.pearson(f1Arr, f2Arr);
                    if (Math.abs(correlation) > maxCorl)
                    {
                        maxCorl = Math.abs(correlation);
                        maxCorlFeature = feature2;
                    }
                }
            }
            if (maxCorlFeature.equals(""))
                 corMap.put(feature1, new Correlation(feature1, 0));
            else
                  corMap.put(feature1, new Correlation(maxCorlFeature, maxCorl));
            }
    }

    public class Correlation
    {
        private String corFeature;
        private float corVal;
        private Correlation(String corFeature, float corVal)
        {
            this.corFeature=corFeature;
            this.corVal = corVal;
        }
        
        public float getCorVal()
        {
            return this.corVal;
        }
        
        public String getCorFeature()
        {
            return this.corFeature;
        }
        
        
    }
}