import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wb.swt.SWTResourceManager;

import tools.Equalizer;

public class paTools {

	protected Shell shlProjectArrhythmiaTools;
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Text txtLol;
	private Text txtFileDestinations;
	private Text txtFileInput;
	private SashForm sashForm_1;
	private Text txtFileOutput;
	private Text txtFileName;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			paTools window = new paTools();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlProjectArrhythmiaTools.open();
		shlProjectArrhythmiaTools.layout();
		while (!shlProjectArrhythmiaTools.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlProjectArrhythmiaTools=new Shell(SWT.CLOSE | SWT.MIN);
		shlProjectArrhythmiaTools.setSize(700, 450);
		shlProjectArrhythmiaTools.setText("Project Arrhythmia Tools v1.0");
		shlProjectArrhythmiaTools.setLayout(new ColumnLayout());
		
		Label titleLabel = new Label(shlProjectArrhythmiaTools, SWT.NONE);
		titleLabel.setLayoutData(new ColumnLayoutData());
		titleLabel.setFont(SWTResourceManager.getFont("Calibri", 16, SWT.NORMAL));
		titleLabel.setAlignment(SWT.CENTER);
		titleLabel.setText("Welcome to this tool made by AirTech.\r\nThis tools lets you create an equalizer out of a .ogg file.");
		
		SashForm sashForm = new SashForm(shlProjectArrhythmiaTools, SWT.NONE);
		sashForm.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		sashForm.setForeground(SWTResourceManager.getColor(192, 192, 192));
		ColumnLayoutData cld_sashForm = new ColumnLayoutData();
		cld_sashForm.heightHint = 30;
		sashForm.setLayoutData(cld_sashForm);
		formToolkit.adapt(sashForm);
		formToolkit.paintBordersFor(sashForm);
		
		txtLol = formToolkit.createText(sashForm, "New Text", SWT.NONE);
		txtLol.setEditable(false);
		txtLol.setEnabled(false);
		txtLol.setBounds(0, 0, 0, 0);
		txtLol.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		txtLol.setText("Equalizer options");
		
		txtFileDestinations = formToolkit.createText(sashForm, "New Text", SWT.NONE);
		txtFileDestinations.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		txtFileDestinations.setBackground(SWTResourceManager.getColor(255, 255, 255));
		txtFileDestinations.setEnabled(false);
		txtFileDestinations.setEditable(false);
		txtFileDestinations.setText("File destinations");
		sashForm.setWeights(new int[] {1, 1});
		
		sashForm_1 = new SashForm(shlProjectArrhythmiaTools, SWT.NONE);
		sashForm_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		ColumnLayoutData cld_sashForm_1 = new ColumnLayoutData();
		cld_sashForm_1.heightHint = 280;
		sashForm_1.setLayoutData(cld_sashForm_1);
		sashForm_1.setSize(690, 400);
		formToolkit.adapt(sashForm_1);
		formToolkit.paintBordersFor(sashForm_1);
		
		Composite composite_1 = formToolkit.createComposite(sashForm_1, SWT.NONE);
		composite_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		formToolkit.paintBordersFor(composite_1);
		
		Label lblNewLabel = formToolkit.createLabel(composite_1, "Refresh rate:", SWT.NONE);
		lblNewLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblNewLabel.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		lblNewLabel.setBounds(61, 10, 84, 19);
		
		Label lblNewLabel_1 = formToolkit.createLabel(composite_1, "Number of bands:", SWT.NONE);
		lblNewLabel_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblNewLabel_1.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		lblNewLabel_1.setBounds(30, 70, 115, 15);
		
		Button radioButtonAll = new Button(composite_1, SWT.RADIO);
		radioButtonAll.setSelection(true);
		radioButtonAll.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		radioButtonAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		radioButtonAll.setBounds(30, 130, 132, 16);
		formToolkit.adapt(radioButtonAll, true, true);
		radioButtonAll.setText("All audio sources");
		radioButtonAll.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		Button radioButtonSpecific = new Button(composite_1, SWT.RADIO);
		radioButtonSpecific.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		radioButtonSpecific.setBounds(30, 152, 157, 16);
		formToolkit.adapt(radioButtonSpecific, true, true);
		radioButtonSpecific.setText("Specific audio source");
		radioButtonSpecific.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		radioButtonSpecific.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		
		Spinner spinnerRefreshRate = new Spinner(composite_1, SWT.BORDER);
		spinnerRefreshRate.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		spinnerRefreshRate.setBounds(160, 10, 75, 22);
        spinnerRefreshRate.setMinimum(1);
        spinnerRefreshRate.setMaximum(10);
        spinnerRefreshRate.setSelection(4);
		formToolkit.adapt(spinnerRefreshRate);
		formToolkit.paintBordersFor(spinnerRefreshRate);
		
		Spinner spinnerNbBands = new Spinner(composite_1, SWT.BORDER);
		spinnerNbBands.setMaximum(60);
		spinnerNbBands.setMinimum(2);
		spinnerNbBands.setSelection(20);
		spinnerNbBands.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		spinnerNbBands.setBounds(160, 70, 75, 22);
		formToolkit.adapt(spinnerNbBands);
		formToolkit.paintBordersFor(spinnerNbBands);
		
		Spinner spinnerAudioSource = new Spinner(composite_1, SWT.BORDER);
		spinnerAudioSource.setMaximum(8);
		spinnerAudioSource.setMinimum(1);
		spinnerAudioSource.setSelection(1);
		spinnerAudioSource.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		spinnerAudioSource.setBounds(193, 148, 75, 22);
		formToolkit.adapt(spinnerAudioSource);
		formToolkit.paintBordersFor(spinnerAudioSource);

		Composite composite = formToolkit.createComposite(sashForm_1, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		formToolkit.paintBordersFor(composite);
		sashForm_1.setWeights(new int[] {1, 1});
		
		Label lblNewLabel2 = formToolkit.createLabel(composite, "Destination folder:", SWT.NONE);
		lblNewLabel2.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblNewLabel2.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		lblNewLabel2.setBounds(10, 120, 121, 19);
		
		txtFileInput = new Text(composite, SWT.NONE);
		txtFileInput.setText("C:\\");
		txtFileInput.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		txtFileInput.setLocation(10, 35);
		txtFileInput.setSize(240, 25);
		Button button = new Button(composite, SWT.PUSH);
		button.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		button.setLocation(256, 35);
		button.setSize(75, 25);
		button.setTouchEnabled(true);
		button.setText("Browse");
		button.addSelectionListener(new SelectionAdapter() {
			private void displayFiles(String[] files) {
				  for (int i = 0; files != null && i < files.length; i++) {
					  txtFileInput.setText(files[i]);
					  txtFileInput.setEditable(true);
				  }
			}
			public void widgetSelected(SelectionEvent e) {
				org.eclipse.swt.widgets.FileDialog dialog = 
						new org.eclipse.swt.widgets.FileDialog(shlProjectArrhythmiaTools, SWT.NULL);
				dialog.setFilterNames(new String[]{"Vorbis OGG (*.ogg)"});
				dialog.setFilterExtensions(new String[]{"*.ogg"});
				String path = dialog.open();
				if (path != null) {
					File file = new File(path);
					if (file.isFile()) displayFiles(new String[] {file.toString()});
					else displayFiles(file.list());
				}
			 }
		});
		
		Label lblNewLabel3 = formToolkit.createLabel(composite, "File name:", SWT.NONE);
		lblNewLabel3.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblNewLabel3.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		lblNewLabel3.setBounds(10, 177, 67, 19);
		
		txtFileOutput = new Text(composite, SWT.NONE);
		txtFileOutput.setText("C:\\");
		txtFileOutput.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		txtFileOutput.setLocation(10, 146);
		txtFileOutput.setSize(240, 25);
		Button button2 = new Button(composite, SWT.PUSH);
		button2.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		button2.setLocation(256, 146);
		button2.setSize(75, 25);
		button2.setTouchEnabled(true);
		button2.setText("Browse");
		button2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlProjectArrhythmiaTools);
		        // Set the initial filter path according
		        // to anything they've selected or typed in
		        dlg.setFilterPath(txtFileOutput.getText());
		        // Change the title bar text
		        dlg.setText("SWT's DirectoryDialog");
		        // Customizable message displayed in the dialog
		        dlg.setMessage("Select a directory");
		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		        String dir = dlg.open();
		        if (dir != null)  txtFileOutput.setText(dir);// Set the text box to the new selection
			}
		});
		Label lblNewLabel4 = formToolkit.createLabel(composite, "Music file:", SWT.NONE);
		lblNewLabel4.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		lblNewLabel4.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		lblNewLabel4.setBounds(10, 10, 67, 19);
		
		txtFileName = new Text(composite, SWT.BORDER);
		txtFileName.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.NORMAL));
		txtFileName.setBounds(83, 177, 167, 21);
		txtFileName.setText("equalizer");
		formToolkit.adapt(txtFileName, true, true);
		
		Button btnNewButton = formToolkit.createButton(shlProjectArrhythmiaTools, "Convert !", SWT.NONE);
		btnNewButton.setFont(SWTResourceManager.getFont("Calibri", 16, SWT.NORMAL));

		btnNewButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				File fileInput = new File(txtFileInput.getText());
				File fileOutput = new File(txtFileOutput.getText());
				if (txtFileName.getText()!=""&&fileInput.isAbsolute()&&fileOutput.isAbsolute()
						&&fileInput.isFile()&&fileOutput.isDirectory()){
					btnNewButton.setEnabled(false);
					Equalizer eq = new Equalizer();
					eq.setFilepath(txtFileInput.getText());
					eq.setDestpath(txtFileOutput.getText()+"\\"+txtFileName.getText()+".lsp");
					if (radioButtonSpecific.getSelection()) eq.setOptions(new int[] {spinnerAudioSource.getSelection()});
					else eq.setOptions(new int[] {0});
					eq.setNbBands(spinnerNbBands.getSelection());
					eq.setRefreshRate(spinnerRefreshRate.getSelection());
					try {
						eq.run();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					new MessageDialog(shlProjectArrhythmiaTools, "", null,
						    "File generated.", MessageDialog.INFORMATION, new String[] {"Ok"}, 0).open();
					btnNewButton.setEnabled(true);
				}
			}
		});
		ColumnLayoutData cld_btnNewButton = new ColumnLayoutData();
		cld_btnNewButton.heightHint = 30;
		cld_btnNewButton.widthHint = 420;
		btnNewButton.setLayoutData(cld_btnNewButton);
	}
}

