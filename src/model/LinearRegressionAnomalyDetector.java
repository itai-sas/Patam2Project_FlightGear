package model;

import utils.CorrelatedFeatures;
import utils.Line;
import utils.Point;
import utils.StatLib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LinearRegressionAnomalyDetector implements TimeSeriesAnomalyDetector
{
	ArrayList<CorrelatedFeatures> corFeatures;
	float corlThreshold;
	TimeSeries normalTs, anomalyTs;
	HashMap<String, HashSet<Integer>> anomalyReports;
	private final LinearRegPainter painter;

	public LinearRegressionAnomalyDetector()
	{
		corFeatures = new ArrayList<>();
		corlThreshold = (float) 0.9;
		painter = new LinearRegPainter();
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
			if (corl > corlThreshold)
			{

				Point ps[] = toPoints(this.normalTs.getFeatureData(feature), this.normalTs.getFeatureData(corFeature));
				Line lin_reg = StatLib.linearReg(ps);
				float threshold = findThreshold(ps, lin_reg) * 1.1f;
																		
				CorrelatedFeatures c = new CorrelatedFeatures(feature, corFeature, corl, lin_reg, threshold);
				corFeatures.add(c);
			}
		}
		painter.corFeatures = this.corFeatures;
	}
	private Point[] toPoints(ArrayList<Float> x, ArrayList<Float> y)
	{
		Point[] ps = new Point[x.size()];
		for (int i = 0; i < ps.length; i++)
			ps[i] = new Point(x.get(i), y.get(i));
		return ps;
	}

	private float findThreshold(Point[] ps, Line rl)
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

	@Override
	public void detect(TimeSeries ts)
	{
		this.anomalyTs = ts;
		this.anomalyReports = new HashMap<>();

		for (CorrelatedFeatures c : corFeatures)
		{
			ArrayList<Float> x = this.anomalyTs.getFeatureData(c.feature1);
			ArrayList<Float> y = this.anomalyTs.getFeatureData(c.feature2);
			for (int i = 0; i < x.size(); i++)
			{
				if (Math.abs(y.get(i) - c.lineReg.f(x.get(i))) > c.threshold)
				{
					String d = c.feature1;

					if (!this.anomalyReports.containsKey(d))
						this.anomalyReports.put(d, new HashSet<>());
					this.anomalyReports.get(d).add(i);
				}
			}
		}
		painter.anomalyReports = this.anomalyReports;
	}
	@Override
	public Painter getPainter()
	{
		if (normalTs != null && anomalyTs != null && anomalyReports != null)
			painter.setAll(normalTs, anomalyTs, anomalyReports);
		return painter;
	}
	public List<CorrelatedFeatures> getNormalModel()
	{
		return corFeatures;
	}
	public float getCorlThreshold()
	{
		return corlThreshold;
	}
	public void setCorlThreshold(float corlThreshold)
	{
		this.corlThreshold = corlThreshold;
	}

}
