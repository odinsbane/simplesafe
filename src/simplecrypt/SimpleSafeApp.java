package simplecrypt;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;

/**
 * Start the simple safe application, which is just a JFrame
 * with a JTextPane for editing/viewing text and a menu bar.
 *
 * User: melkor
 * Date: 2/16/13
 * Time: 2:34 PM
 */
public class SimpleSafeApp {
    JTextPane pane;
    JFrame frame;
    File pwd = new File(".");
    String password;
    ButtonGroup key_sizes;

    private static final double VERSION=0.9;
    private static final String UPDATED="3-18-2013";

    private void buildGUI(){


        frame = new JFrame("Simple Safe");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pane = new JTextPane();
        frame.add(new JScrollPane(pane), BorderLayout.CENTER);

        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("file");
        bar.add(file);

        JMenuItem load = new JMenuItem("load");
        file.add(load);

        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadText();
            }
        });

        JMenuItem save = new JMenuItem("save");
        file.add(save);

        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveText();
            }
        });

        JMenuItem setPassword = new JMenuItem("enter password");
        file.add(setPassword);
        setPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getPassword();
            }
        });

        JMenu key = new JMenu("key size");
        bar.add(key);
        key_sizes = new ButtonGroup();

        JRadioButtonMenuItem onetwentyeight = new JRadioButtonMenuItem("128");
        key.add(onetwentyeight);
        key_sizes.add(onetwentyeight);
        JRadioButtonMenuItem twofiftysix = new JRadioButtonMenuItem("256");
        key.add(twofiftysix);
        key_sizes.add(twofiftysix);
        onetwentyeight.setSelected(true);


        JMenu help = new JMenu("help");
        bar.add(help);

        JMenuItem about = new JMenuItem("about");
        help.add(about);
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutWindow();
            }
        });


        frame.setJMenuBar(bar);
        frame.setSize(new Dimension(600, 600));
        frame.setVisible(true);

    }

    private void showAboutWindow() {
        String s ="<html>" +
                "<body style=\"margin:30px;\"><div>" +
                "<h1>Simple Safe</h1>" +
                "<h2>version: %3.2f</h2>" +
                "<h3>updated: %s </h3>" +
                "<h3><a href=\"https://code.google.com/p/simplesafe/\">simplesafe.googlecode.com</a></h3>"+
                "<p>This program is distributed as is without warranty under the " +
                "<a href=\"http://www.opensource.org/licenses/mit-license.php\">MIT License</a></p>" +
                "</div></body></html>";
        JFrame about_frame = new JFrame("about");
        JEditorPane pane = new JEditorPane("text/html",String.format(s, VERSION, UPDATED));
        pane.setEditable(false);
        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED) return;
                try{
                    Desktop d = Desktop.getDesktop();
                    d.browse(e.getURL().toURI());
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        about_frame.setContentPane(pane);
        about_frame.pack();
        about_frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        about_frame.setVisible(true);


    }


    public void getPassword(){
        password = PasswordDialog.getPassword();
    }

    public void loadText(){
        JFileChooser chooser = new JFileChooser(pwd);
        chooser.showDialog(frame,"Open Encrypted File");
        File input = chooser.getSelectedFile();
        if(input!=null){
            pwd = chooser.getCurrentDirectory();
        } else{
            //cancelled
            return;
        }

        if(password==null){
            getPassword();
        }
        int key_size = getSelectedKeySize();
        SafeCrypt crypt = new SafeCrypt("simple safe", key_size);
        crypt.setPassword(password);
        String text = crypt.decryptFile(input);
        pane.setText(text);

    }

    public int getSelectedKeySize(){

        Enumeration<AbstractButton> buttons = key_sizes.getElements();
        while(buttons.hasMoreElements()){
            AbstractButton b = buttons.nextElement();
            if(b.isSelected()){
                return Integer.parseInt(b.getText());
            }

        }

        //default is 128
        return 128;


    }

    public void saveText(){
        JFileChooser chooser = new JFileChooser("Save Encrypted File");
        chooser.setCurrentDirectory(pwd);
        chooser.showSaveDialog(frame);
        File output = chooser.getSelectedFile();
        if(output!=null){

            pwd = chooser.getCurrentDirectory();
        } else{
            return;
        }
        if(password==null){
            getPassword();
            if(password==null) return;
        }
        SafeCrypt crypt = new SafeCrypt("simple safe", getSelectedKeySize());
        crypt.setPassword(password);

        String text = pane.getText();
        crypt.encryptAndSave(output, text);

    }

    public static void main(String[] args){
        final SimpleSafeApp ssa = new SimpleSafeApp();
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ssa.buildGUI();
            }
        });
    }
}

class PasswordDialog extends JDialog{
    JTextField input_text;
    boolean cancelled = false;

    public PasswordDialog(Frame owner){
        super(owner, true);
    }
    static String getPassword(){

        PasswordDialog jd = new PasswordDialog(JOptionPane.getRootFrame());
        jd.createInterface();
        jd.setVisible(true);
        jd.dispose();
        return jd.cancelled?null:jd.input_text.getText();
    }

    private void createInterface(){
        JPanel content = new JPanel();
        GridBagLayout bgl = new GridBagLayout();
        content.setLayout(bgl);
        input_text = new JPasswordField();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(input_text, gbc);

        JButton cancel = new JButton("cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        content.add(cancel, gbc);
        bgl.setConstraints(cancel,gbc);

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled=true;
                setVisible(false);
            }
        });

        JButton ok = new JButton("ok");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        content.add(ok, gbc);

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled=false;
                setVisible(false);
            }
        });

        setContentPane(content);
        pack();

    }

}