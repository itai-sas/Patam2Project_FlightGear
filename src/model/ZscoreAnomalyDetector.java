package model;
import utils.StatLib;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ZscoreAnomalyDetector implements TimeSeriesAnomalyDetector
{
    ArrayList<Float> zArr;
    TimeSeries normalTs,anomalyTs;
    HashMap<String, HashSet<Integer>> anomalyReports;
    HashMap<String,Float> thresholdMap;
    HashMap<String,ArrayList<Float>> zArrAnomaly;
    private final ZscorePainter painter;

    public ZscoreAnomalyDetector()
    {
        zArr = new ArrayList<>();
        painter = new ZscorePainter();
        thresholdMap = new HashMap<>();
        zArrAnomaly = new HashMap<>();
    }
    @Override
    public void learnNormal(TimeSeries ts)
    {
        this.normalTs =ts;
        ArrayList<String> featuresNames = this.normalTs.getFeatures();
        for(int i=0;i<featuresNames.size();i++)
        {
            ArrayList<Float> ftCol = this.normalTs.getFeatureData(featuresNames.get(i));
            String curr= featuresNames.get(i);
            float maxTh=0,currAvg,currStd,currZscore;
            for(int j=1;j<ftCol.size();j++)
            {
                float[] arr = new float[j];
                for(int k=0;k<j;k++)
                {
                    arr[k] = ftCol.get(k);
                }
                currAvg = StatLib.avg(arr);
                currStd = (float) Math.sqrt(StatLib.var(arr));
                currZscore = zScore(ftCol.get(j),currAvg,currStd);
                maxTh = Math.max(currZscore, maxTh);
            }

            zArr.add(maxTh);
            thresholdMap.put(curr,maxTh);
        }
        painter.thresholdMap = this.thresholdMap;
    }
    @Override
    public void detect(TimeSeries ts)
    {
        this.anomalyTs=ts;
        this.anomalyReports = new HashMap<>();
        ArrayList<String> featuresNames = this.anomalyTs.getFeatures();
        for(int i=0;i<featuresNames.size();i++)
        {
            ArrayList<Float> ftCol = this.anomalyTs.getFeatureData(featuresNames.get(i));
            String curr= featuresNames.get(i);
            float currAvg,currStd,currZscore;
            for(int j=1;j<ftCol.size();j++)
            {
                float[] arr = new float[j];

                for(int k=0;k<j;k++)
                {
                    arr[k] = ftCol.get(k);
                }
                currAvg = StatLib.avg(arr);
                currStd = (float) Math.sqrt(StatLib.var(arr));
                currZscore = zScore(ftCol.get(j),currAvg,currStd);
                if (!zArrAnomaly.containsKey(curr))
                {
                    zArrAnomaly.put(curr, new ArrayList<>());
                }
                zArrAnomaly.get(curr).add(currZscore);
                if(currZscore>zArr.get(i))
                {
                	if(!this.anomalyReports.containsKey(featuresNames.get(i)))
                		this.anomalyReports.put(featuresNames.get(i), new HashSet<>());
                	this.anomalyReports.get(featuresNames.get(i)).add(j);
                }
            }
        }
        painter.zArrAnomalyMap = this.zArrAnomaly;
        painter.anomalyReports = this.anomalyReports;
    }
    @Override
    public Painter getPainter()
    {
        if(normalTs!=null&&anomalyTs!=null&&anomalyReports!=null)
            painter.setAll(normalTs,anomalyTs,anomalyReports);
        return painter;
    }

    private float zScore(float val,float avg, float stdev)
    {
        if(stdev==0) return 0;
        return (Math.abs(val-avg) / stdev);
    }
}
