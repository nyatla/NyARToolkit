package jp.nyatla.nyartoolkit.apps.nftfilegen;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.JButton;
import javax.swing.JTextArea;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;



import javax.swing.JScrollPane;


import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.JComboBox;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.DefaultComboBoxModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.nyatla.nyartoolkit.apps.nftfilegen.cmd.FileOpen;
import jp.nyatla.nyartoolkit.apps.nftfilegen.cmd.MakeFeature;
import jp.nyatla.nyartoolkit.core.NyARVersion;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftDataSetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile.NyAR2FeatureCoord;


import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;


public class NyarNftGenApp extends JFrame {

	private static final long serialVersionUID = -431750214737784092L;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NyarNftGenApp frame = new NyarNftGenApp();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * プレビューパネル
	 * @author nyatla
	 *
	 */
	class PreviewPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		public BufferedImage _img=null;
		private BufferedImage _dst_img=null;
		public void setImage(BufferedImage i_img)
		{
			this._img=i_img;
		}
		public void setDestImage(BufferedImage i_img)
		{
			this._dst_img=i_img;
		}
	    @Override
	    public void paintComponent(Graphics g) {
	        Graphics2D g2D = (Graphics2D) g;
	        if(this._img==null){
	        	g2D.setColor(Color.BLACK);
	        	g2D.setStroke(new BasicStroke(5));
	        	g2D.drawRect(0,0,this.getWidth()-1,this.getHeight()-1);
	        }else if(this._dst_img==null){
		        double iw = this._img.getWidth(null);
		        double ih = this._img.getHeight(null);
		        double pw = this.getWidth();
		        double ph = this.getHeight();
		        double ppx=pw/iw;
		        double ppy=ph/ih;
		        double pp=ppx<ppy?ppx:ppy;
		        AffineTransform af = AffineTransform.getScaleInstance(pp,pp);
		        g2D.drawImage(this._img, af, this);
	        }else{
		        double iw = this._dst_img.getWidth(null);
		        double ih = this._dst_img.getHeight(null);
		        double pw = this.getWidth();
		        double ph = this.getHeight();
		        double ppx=pw/iw;
		        double ppy=ph/ih;
		        double pp=ppx<ppy?ppx:ppy;
		        AffineTransform af = AffineTransform.getScaleInstance(pp,pp);
		        g2D.drawImage(this._dst_img, af, this);
	        	
	        }
	    }		
	}
	public static double[] parseDpis(String i_s)
	{
		String[] s=i_s.split(",");
		double[] d=new double[s.length];
		try{
			for(int i=0;i<d.length;i++){
				d[i]=Double.parseDouble(s[i]);
			}
		}catch(NumberFormatException e){
			return null;
		}
		return d;
	}
	private static void saveToFile(String i_fname,byte[] i_data) throws IOException
	{
		FileOutputStream output=new FileOutputStream(i_fname);
		output.write(i_data,0,i_data.length);
		output.flush();
		output.close();		
	}

	/**
	 * Create the frame.
	 */
	public NyarNftGenApp()
	{
		setTitle("NyAR NftFileGenerator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 764, 588);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel preview_panel = new PreviewPanel();
		getContentPane().add(preview_panel, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		getContentPane().add(menuBar, BorderLayout.NORTH);
		
		JMenu mnImport = new JMenu("Import");
		menuBar.add(mnImport);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Load image");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BufferedImage _source_bmp=_cmd_fp.openImage();
					if(_source_bmp!=null){
						_preview_panel.setImage(_source_bmp);
						_preview_panel.setDestImage(null);
						_preview_panel.repaint();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		mnImport.add(mntmNewMenuItem);
		
		JMenu mnExport = new JMenu("Export");
		menuBar.add(mnExport);
		
		//Save to File
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Save FeatureSet files");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(_last_result==null){
					JOptionPane.showMessageDialog(null, "Make feature set before to export.");
					return;
				}
				File fp=_cmd_fp.saveFile(null);
				try {
					saveToFile(fp.getAbsoluteFile()+".iset",_last_result.iset.makeIsetBinary());
					saveToFile(fp.getAbsoluteFile()+".fset",_last_result.fset.makeFsetBinary());
					saveToFile(fp.getAbsoluteFile()+".fset3",_last_result.fset3.makeFset3Binary());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "I/O error");
				}
			}
		});
		mnExport.add(mntmNewMenuItem_1);
		
		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Save NyARTK NFT dataset file");
		mntmNewMenuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(_last_result==null){
					JOptionPane.showMessageDialog(null, "Make feature set before to export.");
					return;
				}
				FileNameExtensionFilter[] lf=new FileNameExtensionFilter[]{new FileNameExtensionFilter("NyARTK NFT dataset", "nftdataset")};
				File fp=_cmd_fp.saveFile(lf);
				NyARNftDataSetFile fpack=new NyARNftDataSetFile(_last_result.iset,_last_result.fset,_last_result.fset3);
				try {
					saveToFile(fp.getAbsoluteFile()+".nftdataset",fpack.makeBinary());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "I/O error");
				}			
			}
		});
		mnExport.add(mntmNewMenuItem_2);
		
		JMenu mnNewMenu = new JMenu("Help");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem_3 = new JMenuItem("About");
		mntmNewMenuItem_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,String.format("NyarNftgenApp \n%s",NyARVersion.VERSION_STRING));				
			}
		});
		mnNewMenu.add(mntmNewMenuItem_3);

		
		JPanel sub_panel = new JPanel();
		sub_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(sub_panel, BorderLayout.EAST);
		sub_panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("256px:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		final ProgressDialog dialog=new ProgressDialog(this);	

		this._preview_panel=(PreviewPanel)preview_panel;
		
				
		JButton btnNewButton = new JButton("Make Feature Set");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if(_preview_panel._img==null){
					JOptionPane.showMessageDialog(null, "An image is not loaded.");
					return;
				}
				BufferedImage srcimg=_preview_panel._img;
	
				double dpi=(Integer)_dpi_spinner.getValue();
				double[] sub_dpis=null;
				int iset=_isetdpi_cmb.getSelectedIndex();
				if(iset==_isetdpi_cmb.getItemCount()-1){
					iset=-1;
					sub_dpis=parseDpis(_subdpi_text.getText());
					if(sub_dpis==null){
						JOptionPane.showMessageDialog(null, "Custom Sub DPIs must be CSV doubles.");
						return;
					}
				}
				int lv=_fsetlv_cmb.getSelectedIndex();
				_result_text.setText("");
				MakeFeature mfc=new MakeFeature();
				mfc.execute(
						srcimg, dpi, lv, sub_dpis,
					new MakeFeature.LogOvserver() {
					@Override
					public void onLog(String i_string)
					{	
	
						_result_text.setText(_result_text.getText()+i_string);
					}
					@Override
					public void onFinished(MakeFeature.Result i_result) {
						dialog.breakModal();
						_last_result=i_result;
					}
				});
				//処理中
				dialog.doModal();
				if(dialog.isCanceled()){
					mfc.interrupt();
					return;//中断したらここでおしまい
				}
				//処理完了。解析画像の出力(下位は表示しない)
				{
					BufferedImage d=new BufferedImage(srcimg.getWidth(),srcimg.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
					Graphics g=d.getGraphics();
					g.drawImage(srcimg,0,0,d.getWidth(),d.getHeight(),null);
					((Graphics2D) g).setStroke(new BasicStroke(2));
					for(int i=0;i<1/*_last_result.fset.list.length*/;i++){
						int scale=10;//_last_result.fset.list[i].scale;
						g.setColor(Color.RED);
						for(NyAR2FeatureCoord j: _last_result.fset.list[i].coord){
							g.drawRect(j.x-scale/2,j.y-scale/2,scale,scale);
						}
					}
					g.setColor(Color.BLUE);
					for(int i=0;i<_last_result.fset3.ref_point.length;i++){
						NyARNftFreakFsetFile.RefDataSet ri=_last_result.fset3.ref_point[i];
						if(ri.refImageNo!=0){
							continue;
						}
						g.drawOval((int)(ri.coord2D.x-5/2),(int)(ri.coord2D.y-5/2),5,5);
					}

					_preview_panel.setDestImage(d);
					_preview_panel.repaint();					
				}
			}
		});		
				
		sub_panel.add(btnNewButton, "1, 2, fill, center");
		
		/////////
		
		this._do_make=btnNewButton;
		
		JLabel lblImageDpi = new JLabel("Source DPI");
		sub_panel.add(lblImageDpi, "1, 4");
		
		JSpinner dpi_spinner = new JSpinner();
		
		dpi_spinner.setModel(new SpinnerNumberModel(120, null, 9999, 1));
		sub_panel.add(dpi_spinner, "1, 6");
		this._dpi_spinner=dpi_spinner;
		
		JLabel lblNewLabel = new JLabel("Iset DPIs");
		sub_panel.add(lblNewLabel, "1, 9");
		
		JComboBox<String> isetdpi_cmb = new JComboBox<String>();
		isetdpi_cmb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_subdpi_text.setEnabled(_fsetlv_cmb.getSelectedIndex()==1);
			}
		});
		isetdpi_cmb.setModel(new DefaultComboBoxModel<String>(new String[] {"Auto", "Custom"}));
		sub_panel.add(isetdpi_cmb, "1, 11, fill, default");
		this._isetdpi_cmb=isetdpi_cmb;
		
		JTextField subdpi_text = new JTextField();
		subdpi_text.setText("100,90,80,60,40,20,10");
		subdpi_text.setEnabled(false);
		sub_panel.add(subdpi_text, "1, 13, fill, default");
		subdpi_text.setColumns(10);
		this._subdpi_text=subdpi_text;
		
		JLabel lblFsetParametor = new JLabel("FSET parametor");
		sub_panel.add(lblFsetParametor, "1, 15");
		
		JComboBox<String> fsetlv_cmb = new JComboBox<String>();
		fsetlv_cmb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		fsetlv_cmb.setModel(new DefaultComboBoxModel<String>(new String[] {"Subset LV1", "Subset LV2", "Subset LV3", "Subset LV4"}));
		fsetlv_cmb.setSelectedIndex(1);
		sub_panel.add(fsetlv_cmb, "1, 17, fill, default");
		this._fsetlv_cmb=fsetlv_cmb;
		

		JLabel lblParameteors = new JLabel("Result");
		sub_panel.add(lblParameteors, "1, 21");
		
		JScrollPane scrollPane = new JScrollPane();
		sub_panel.add(scrollPane, "1, 22, 1, 16, fill, fill");
		
		JTextArea result_text = new JTextArea();
		scrollPane.setViewportView(result_text);
		result_text.setTabSize(4);
		this._result_text=result_text;
	}
	JButton _do_make;
	FileOpen _cmd_fp=new FileOpen(this);
	final PreviewPanel _preview_panel;

	final JTextField _subdpi_text;
	final JComboBox<String> _fsetlv_cmb;
	final JComboBox<String> _isetdpi_cmb;
	final JSpinner _dpi_spinner;
	final JTextArea _result_text;
	
	MakeFeature.Result _last_result;
	

}
