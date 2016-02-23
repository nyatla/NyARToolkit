package jp.nyatla.nyartoolkit.base.attoolkit5.kpm.vision.matchers;

public class match_t
{
    public match_t(){
    	this.ins=-1;
    	this.ref=-1;
    }
    public void set(match_t v) {
    	this.ins=v.ins;
    	this.ref=v.ref;
    }
    public void set(int _ins, int _ref) {
    	this.ins=_ins;
    	this.ref=_ref;
	}
	public match_t(int _ins, int _ref){
    	this.ins=_ins;
    	this.ref=_ref;
    }
    public int ins;
    public int ref;

};