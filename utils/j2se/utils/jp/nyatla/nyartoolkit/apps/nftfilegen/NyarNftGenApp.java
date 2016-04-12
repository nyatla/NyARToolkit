package jp.nyatla.nyartoolkit.apps.nftfilegen;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.BoxLayout;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Stroke;

import javax.swing.SpringLayout;
import javax.swing.JScrollPane;
import javax.swing.JLayeredPane;
import java.awt.Component;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.CardLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JProgressBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import jp.nyatla.nyartoolkit.apps.nftfilegen.cmd.FileOpen;
import jp.nyatla.nyartoolkit.apps.nftfilegen.cmd.MakeFeature;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.LineBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextPane;
import javax.swing.JEditorPane;
import java.awt.Rectangle;

public class NyarNftGenApp extends JFrame {

	private static final long serialVersionUID = -431750214737784092L;
	private JTextField textField;

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
		public void setImage(BufferedImage i_img)
		{
			this._img=i_img;
		}
	    @Override
	    public void paintComponent(Graphics g) {
	        Graphics2D g2D = (Graphics2D) g;
	        if(this._img==null){
	        	g2D.setColor(Color.BLACK);
	        	g2D.setStroke(new BasicStroke(5));
	        	g2D.drawRect(0,0,this.getWidth()-1,this.getHeight()-1);
	        }else{
		        double iw = this._img.getWidth(null);
		        double ih = this._img.getHeight(null);
		        double pw = this.getWidth();
		        double ph = this.getHeight();
		        double ppx=pw/iw;
		        double ppy=ph/ih;
		        double pp=ppx<ppy?ppx:ppy;
		        AffineTransform af = AffineTransform.getScaleInstance(pp,pp);
		        g2D.drawImage(this._img, af, this);
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

	/**
	 * Create the frame.
	 */
	public NyarNftGenApp()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 764, 588);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel preview_panel = new PreviewPanel();
		getContentPane().add(preview_panel, BorderLayout.CENTER);
		
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
		
		JButton btnLoadImage = new JButton("Load Image");
		btnLoadImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BufferedImage _source_bmp=_cmd_fp.openImage();
					if(_source_bmp!=null){
						_preview_panel.setImage(_source_bmp);
						_preview_panel.repaint();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		final ProgressDialog dialog=new ProgressDialog(this);	
		sub_panel.add(btnLoadImage, "1, 2");
		
				
				JButton btnNewButton = new JButton("Make Feature");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						if(_preview_panel._img==null){
							JOptionPane.showMessageDialog(null, "An image is not loaded.");
							return;
						}
						double dpi=(int)_dpi_spinner.getValue();
						double[] sub_dpis=null;
						int lv=_subdpi_cmb.getSelectedIndex();
						if(lv==_subdpi_cmb.getItemCount()-1){
							lv=-1;
							sub_dpis=parseDpis(_subdpi_text.getText());
							if(sub_dpis==null){
								JOptionPane.showMessageDialog(null, "Custom Sub DPIs must be CSV doubles.");
								return;
							}
						}
						MakeFeature mfc=new MakeFeature();
						mfc.execute(
							_preview_panel._img, dpi, lv, sub_dpis,
							new MakeFeature.LogOvserver() {
							@Override
							public void onLog(String i_string)
							{	

								_result_text.setText(_result_text.getText()+i_string);
							}
							@Override
							public void onFinished() {
								dialog.setVisible(false);								
							}
						});
						//処理中
//						JOptionPane.showConfirmDialog(null,"Running...","NyARNftgenApp",JOptionPane.CANCEL_OPTION);
						dialog.doModal();
						if(dialog.isCanceled()){
							mfc.interrupt();
						}
					}
				});		
				
		sub_panel.add(btnNewButton, "1, 4, fill, center");
		
		/////////
		
		this._do_make=btnNewButton;
		
		JLabel lblImageDpi = new JLabel("Source DPI");
		sub_panel.add(lblImageDpi, "1, 6");
		
		JSpinner dpi_spinner = new JSpinner();
		
				dpi_spinner.setModel(new SpinnerNumberModel(120, null, 9999, 1));
				sub_panel.add(dpi_spinner, "1, 9");
				this._dpi_spinner=dpi_spinner;
		
		JLabel lblNewLabel = new JLabel("DPI set");
		sub_panel.add(lblNewLabel, "1, 11");
		
		JComboBox subdpi_cmb = new JComboBox();
		subdpi_cmb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_subdpi_text.setEnabled(_subdpi_cmb.getSelectedIndex()==_subdpi_cmb.getItemCount()-1);
			}
		});
		subdpi_cmb.setModel(new DefaultComboBoxModel(new String[] {"Subset LV1", "Subset LV2", "Subset LV3", "Subset LV4", "Custom"}));
		sub_panel.add(subdpi_cmb, "1, 13, fill, default");
		this._subdpi_cmb=subdpi_cmb;
		
		JLabel status_lavel = new JLabel("Idle");
		getContentPane().add(status_lavel, BorderLayout.SOUTH);
		this._preview_panel=(PreviewPanel)preview_panel;
		
		JTextField subdpi_text = new JTextField();
		subdpi_text.setText("100,90,80,60,40,20,10");
		subdpi_text.setEnabled(false);
		sub_panel.add(subdpi_text, "1, 15, fill, default");
		subdpi_text.setColumns(10);
		this._subdpi_text=subdpi_text;
		

		
//		textField_1 = new JTextField();
//		sub_panel.add(textField_1, "1, 13, fill, default");
//		textField_1.setColumns(10);

		JLabel lblParameteors = new JLabel("Result");
		sub_panel.add(lblParameteors, "1, 19");
		
		JScrollPane scrollPane = new JScrollPane();
		sub_panel.add(scrollPane, "1, 21, 1, 17, fill, fill");
		
		JTextArea result_text = new JTextArea();
		scrollPane.setViewportView(result_text);
		result_text.setTabSize(4);
		this._result_text=result_text;
	}
	JButton _do_make;
	JButton _load_img;
	FileOpen _cmd_fp=new FileOpen(this);
	final PreviewPanel _preview_panel;

	final JTextField _subdpi_text;
	final JComboBox _subdpi_cmb;
	final JSpinner _dpi_spinner;
	final JTextArea _result_text;
	
	public void setGuiStatus(int i_mode)
	{
		switch(i_mode)
		{
		case 0:
			this._do_make.setEnabled(false);
			break;
		case 1:
			this._do_make.setEnabled(true);
			break;
		}
	}
}
