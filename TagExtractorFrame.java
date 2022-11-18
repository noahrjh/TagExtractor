import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagExtractorFrame extends JFrame
{
    JPanel mainPnl;
    JPanel displayPnl;
    JPanel btnPnl;

    JButton writeBtn;
    JButton choostBtn;
    JButton quitBtn;

    JTextArea area;

    JScrollPane scrollPane;

    JLabel filenameLbl;

    private Set<String> set = new HashSet<>();
    private Map<String, Integer> map = new HashMap<>();

    public TagExtractorFrame()
    {
        mainPnl = new JPanel();
        mainPnl.setLayout(new BorderLayout());

        createBtnPnl();
        createDisplayPnl();

        mainPnl.add(displayPnl, BorderLayout.CENTER);
        mainPnl.add(btnPnl, BorderLayout.SOUTH);

        add(mainPnl);

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;

        setSize(screenWidth / 2, screenHeight / 2);
        setLocation(screenWidth / 4, screenHeight / 4);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void createBtnPnl()
    {
        btnPnl = new JPanel();
        btnPnl.setLayout(new GridLayout(1,3));

        choostBtn = new JButton("Choose");
        writeBtn = new JButton("Write");
        quitBtn = new JButton("Quit");

        choostBtn.setFont(new Font("Arial", Font.BOLD, 24));
        writeBtn.setFont(new Font("Arial", Font.BOLD, 24));
        quitBtn.setFont(new Font("Arial", Font.BOLD, 24));

        choostBtn.addActionListener((ActionEvent e)->{pickFile();});
        writeBtn.addActionListener((ActionEvent e)->{writeFile();});
        quitBtn.addActionListener((ActionEvent e)->{System.exit(0);});

        btnPnl.add(choostBtn);
        btnPnl.add(writeBtn);
        btnPnl.add(quitBtn);
    }

    public void createDisplayPnl()
    {
        displayPnl = new JPanel();
        displayPnl.setBorder(new TitledBorder(new EtchedBorder(), "Tag Extractor"));
        displayPnl.setLayout(new BorderLayout());

        filenameLbl = new JLabel("Chosen File: ");
        filenameLbl.setHorizontalAlignment(JLabel.CENTER);
        filenameLbl.setFont(new Font("Arial", Font.PLAIN, 18));
        area = new JTextArea();
        area.setFont(new Font("Arial", Font.PLAIN, 18));
        area.setEditable(false);
        scrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        displayPnl.setBackground(Color.white);
        displayPnl.add(filenameLbl, BorderLayout.NORTH);
        displayPnl.add(scrollPane, BorderLayout.CENTER);
    }

    public void pickFile()
    {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";
        ArrayList words = new ArrayList<>();
        try
        {
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                InputStream in =
                        new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));
                int line = 0;
                filenameLbl.setText("Chosen File:   " + selectedFile.getName());
                JOptionPane.showMessageDialog(displayPnl, "Choose a Stop Word Filter File", "Choose a File", JOptionPane.INFORMATION_MESSAGE);
                chooseStopWordFilterList();
                Scanner scan = new Scanner(selectedFile);
                while(scan.hasNext())
                {
                    words.add(scan.next().replace("\t", "").replace(" ", "").replace("-", "").replace("!", "").replace(".", "").replace(",", "").replace("\t\t", "").trim().toLowerCase());
                }
                for (int i = 0; i < words.size(); i++)
                {
                    if(set.contains((String) words.get(i)) == true)
                    {
                        words.remove(i);
                        i--;
                    } else if (((String) words.get(i)).length() < 3)
                    {
                        words.remove(i);
                        i--;
                    }
                }
                map = (Map<String, Integer>) words.parallelStream().collect(Collectors.groupingByConcurrent(w -> w,
                        Collectors.counting()));
                for(Map.Entry<String, Integer> en: map.entrySet())
                {
                    area.append("Word:\t" + en.getKey() + "\t\tFrequency:\t" + en.getValue() + "\n");
                }
                reader.close(); // must close the file to seal it and flush buffer

            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found!!!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void chooseStopWordFilterList()
    {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String rec = "";
        try
        {
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                InputStream in =
                        new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));
                Scanner scanner = new Scanner(selectedFile);
                while(scanner.hasNext())
                {
                    rec = scanner.next();
                    set.add(rec.toLowerCase());
                }
                reader.close();
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found!!!");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void writeFile()
    {
        File workingDirectory = new File(System.getProperty("user.dir"));
        Path file = Paths.get(workingDirectory.getPath() + "\\src\\filteredlist.txt");
        try
        {
            OutputStream out =
                    new BufferedOutputStream(Files.newOutputStream(file, CREATE));
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(out));


            writer.write(area.getText());
            JOptionPane.showMessageDialog(displayPnl,"File has been written!", "File", JOptionPane.INFORMATION_MESSAGE);
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
