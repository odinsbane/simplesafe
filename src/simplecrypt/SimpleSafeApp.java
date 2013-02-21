package simplecrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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

        frame.setJMenuBar(bar);
        frame.setSize(new Dimension(600, 600));
        frame.setVisible(true);

    }

    public void getPassword(){
        password = JOptionPane.showInputDialog("Enter Password");
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

        SafeCrypt crypt = new SafeCrypt("simple safe");
        crypt.setPassword(password);
        String text = crypt.decryptFile(input);
        pane.setText(text);

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
        }

        SafeCrypt crypt = new SafeCrypt("simple safe");
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
 