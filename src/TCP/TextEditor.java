package TCP;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

public class TextEditor extends JFrame {
    private JTextArea t;
    private JFrame f;

    TextEditor() {
        // Create a frame
        f = new JFrame("editor");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Text component
        t = new JTextArea(20,40);

        f.add(t);
        f.pack();
        f.setVisible(true);
    }

    int getCursorPosition() {
        return t.getCaretPosition();
    }

    void setCursorPosition(int position) {
        if (position <= t.getText().length()) {
            t.setCaretPosition(position);
        } else {
            t.setCaretPosition(t.getText().length());
        }
    }

    String getText() {
        return t.getText();
    }

    void setText(String text) {
        t.setText(text);
    }

    public JTextArea getT() {
        return t;
    }

    public void setT(JTextArea t) {
        this.t = t;
    }

    public static void main(String[] args) {
        new TextEditor();
    }
}
