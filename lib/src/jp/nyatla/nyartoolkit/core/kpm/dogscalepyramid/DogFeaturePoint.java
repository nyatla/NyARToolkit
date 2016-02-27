package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;


public class DogFeaturePoint {
	public double x, y;
	public double angle;
	public int octave;
	public int scale;
	public double sp_scale;
	public double score;
	public double sigma;
	public double edge_score;

	public DogFeaturePoint() {
	}

	public DogFeaturePoint(DogFeaturePoint i_src) {
		this.set(i_src);
	}

	public void set(DogFeaturePoint i_src) {
		this.x = i_src.x;
		this.y = i_src.y;
		this.angle = i_src.angle;
		this.octave = i_src.octave;
		this.scale = i_src.scale;
		this.sp_scale = i_src.sp_scale;
		this.score = i_src.score;
		this.sigma = i_src.sigma;
		this.edge_score = i_src.edge_score;
	}

}; // FeaturePoint
