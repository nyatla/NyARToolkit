package jp.nyatla.nyartoolkit.apps.nftfilegen;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class ProgressDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 286204141525247015L;
	private final JPanel contentPanel = new JPanel();


	/**
	 * Create the dialog.
	 */
	public ProgressDialog(Frame owner) {
		super(owner,true);
		final JDialog _t=this;
		this.setSize(295, 162);
//		setBounds(100, 100, 295, 162);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JLabel lblCalculatingTheFeature = new JLabel("Extracting feature point set ...");
			lblCalculatingTheFeature.setHorizontalAlignment(SwingConstants.CENTER);
			lblCalculatingTheFeature.setFont(new Font("MS UI Gothic", Font.PLAIN, 16));
			contentPanel.add(lblCalculatingTheFeature, BorderLayout.CENTER);
		}
		{
			JLabel label = new JLabel("");
			label.setIcon(new ImageIcon(ProgressDialog.class.getResource("/jp/nyatla/nyartoolkit/apps/nftfilegen/hourglass.gif")));
			contentPanel.add(label, BorderLayout.EAST);
		}

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_t.setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}		
	}
	public void breakModal()
	{
		this.is_canceled=false;
		this.setVisible(false);
	}
	public void doModal() {
		this.is_canceled=true;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		return;
	}
	public boolean isCanceled(){
		return this.is_canceled;
	}
	private boolean is_canceled;

}
