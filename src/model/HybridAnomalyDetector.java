package model;

import other.*;

import java.util.*;

public class HybridAnomalyDetector implements TimeSeriesAnomalyDetector
{
	
	TimeSeries normalTs, anomalyTs;
	HashMap<String, HashSet<Integer>> anomalyReports;
	HashMap<String, Float> featureToCorl;
	private final HybridPainter painter;
	ArrayList<CorrelatedFeatures> corFeatures;
	LinkedHashMap<String, Float> zMap;
	HashMap<String,ArrayList<Float>> zArrAnomaly;
	LinkedHashMap<String, Circle> wMap;

	public HybridAnomalyDetector()
	{
		corFeatures = new ArrayList<>();
		zMap = new LinkedHashMap<>();
		wMap = new LinkedHashMap<>();
		featureToCorl = new HashMap<>();
		zArrAnomaly = new HashMap<>();
		painter = new HybridPainter();
	}

	@Override
	public void learnNormal(TimeSeries ts)
	{
		this.normalTs = ts;
		ArrayList<String> ft = this.normalTs.getFeatures(); 

		for (String feature : ft)
		{
			float corl = this.normalTs.getCorMap().get(feature).getCorVal();
			String corFeature = this.normalTs.getCorMap().get(feature).getCorFeature();
			featureToCorl.put(feature, corl);
			if (corl >= (float) 0.95)
			{

				Point ps[] = toPoints(this.normalTs.getFeatureData(feature), this.normalTs.getFeatureData(corFeature));
				Line lin_reg = StatLib.linearReg(ps);
				float threshold = findThreshold(ps, lin_reg) * 1.1f;
				CorrelatedFeatures c = new CorrelatedFeatures(feature, corFeature, corl, lin_reg, threshold);
				corFeatures.add(c);
			} else if (corl < (float) 0.5)
			{

				float maxTh = 0, currAvg, currStd, currZscore;
				for (int i = 1; i < this.normalTs.getFeatureData(feature).size(); i++)
				{
					float[] arr = new float[i];

					for (int j = 0; j < i; j++)
					{
						arr[j] = this.normalTs.getFeatureData(feature).get(j);
					}
					currAvg = StatLib.avg(arr);
					currStd = (float) Math.sqrt(StatLib.var(arr));
					currZscore = zScore(this.normalTs.getFeatureData(feature).get(i), currAvg, currStd);
					maxTh = Math.max(currZscore, maxTh);
				}
				zMap.put(feature, maxTh);
			}
			else
			{
				ArrayList<Point> ps = toPointsArrayList(this.normalTs.getFeatureData(feature),
						this.normalTs.getFeatureData(corFeature));
				Circle wCircle = Welzl.makeCircle(ps);
				wMap.put(feature, wCircle);
			}

		}

	}
	@Override
	public void detect(TimeSeries ts)
	{
		this.anomalyTs = ts;
		anomalyReports = new HashMap<>();
		for (CorrelatedFeatures c : corFeatures)
		{
			ArrayList<Float> x = this.anomalyTs.getFeatureData(c.feature1);
			ArrayList<Float> y = this.anomalyTs.getFeatureData(c.feature2);
			for (int i = 0; i < x.size(); i++) {
				if (Math.abs(y.get(i) - c.lineReg.f(x.get(i))) > c.threshold)
				{
					String d = c.feature1;
					if (!this.anomalyReports.containsKey(d))
						this.anomalyReports.put(d, new HashSet<>());
					this.anomalyReports.get(d).add(i);

				}
			}
		}
		ArrayList<String> zFeaturesNames = new ArrayList<String>(zMap.keySet());
		for (int i = 0; i < zMap.size(); i++)
		{
			ArrayList<Float> ftCol = this.anomalyTs.getFeatureData(zFeaturesNames.get(i));
			float currAvg, currStd, currZscore;
			for (int j = 1; j < ftCol.size(); j++)
			{
				float[] arr = new float[j];
				for (int k = 0; k < j; k++)
				{
					arr[k] = ftCol.get(k);
				}
				currAvg = StatLib.avg(arr);
				currStd = (float) Math.sqrt(StatLib.var(arr));
				currZscore = zScore(ftCol.get(j), currAvg, currStd);
				 if (!zArrAnomaly.containsKey(zFeaturesNames.get(i))) {
	                    zArrAnomaly.put(zFeaturesNames.get(i), new ArrayList<>());
	                }
	                zArrAnomaly.get(zFeaturesNames.get(i)).add(currZscore);
				if (currZscore > zMap.get(zFeaturesNames.get(i)))
				{
					if (!this.anomalyReports.containsKey(zFeaturesNames.get(i)))
						this.anomalyReports.put(zFeaturesNames.get(i), new HashSet<>());
					this.anomalyReports.get(zFeaturesNames.get(i)).add(j);
				}
			}
		}
		for (String s : wMap.keySet())
		{
			String corFeature = this.normalTs.getCorMap().get(s).getCorFeature();
			ArrayList<Float> col1Arr = this.anomalyTs.getFeatureData(s);
			ArrayList<Float> col2Arr = this.anomalyTs.getFeatureData(corFeature);
			ArrayList<Point> ps = toPointsArrayList(col1Arr, col2Arr);
			for (int j = 0; j < ps.size(); j++)
			{
				if (!wMap.get(s).isContained(ps.get(j)))
				{
					if (!this.anomalyReports.containsKey(s))
						this.anomalyReports.put(s, new HashSet<>());
					this.anomalyReports.get(s).add(j); 
				}
			}
		}
		painter.anomalyReports = this.anomalyReports;
		painter.zArrAnomaly = this.zArrAnomaly;
		painter.zMap = this.zMap;
		painter.wMap = this.wMap;
		painter.featureToCorl = this.featureToCorl;
		painter.corFeatures = this.corFeatures;
	}
	@Override
	public Painter getPainter()
	{
		if (normalTs != null && anomalyTs != null && anomalyReports != null)
			painter.setAll(normalTs, anomalyTs, anomalyReports);
		return painter;
	}
	private Point[] toPoints(ArrayList<Float> x, ArrayList<Float> y)
	{
		Point[] ps = new Point[x.size()];
		for (int i = 0; i < ps.length; i++)
			ps[i] = new Point(x.get(i), y.get(i));
		return ps;
	}

	private ArrayList<Point> toPointsArrayList(ArrayList<Float> x, ArrayList<Float> y)
	{
		ArrayList<Point> ps = new ArrayList<>();
		for (int i = 0; i < x.size(); i++)
			ps.add(new Point(x.get(i), y.get(i)));
		return ps;
	}

	private float findThreshold(Point ps[], Line rl)
	{
		float max = 0;
		for (int i = 0; i < ps.length; i++)
		{
			float d = Math.abs(ps[i].y - rl.f(ps[i].x));
			if (d > max)
				max = d;
		}
		return max;
	}
	private float zScore(float val, float avg, float stdev)
	{
		if (stdev == 0)
			return 0;
		return (Math.abs(val - avg) / stdev);
	}
	public List<CorrelatedFeatures> getNormalModel()
	{
		return corFeatures;
	}

}
