package AwtSwing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class BingoGame {
	static public class MyFrame extends JFrame implements ActionListener {
		JPanel panelNorth; // 메뉴 화면
		JPanel panelWest; // 점수 화면
		JPanel panelCenter; // 시도 횟수 화면
		JPanel panelSouth; // 게임 화면
		JLabel labelMessage; // 메뉴 메시지
		JLabel labelScore; // 점수 메시지
		JLabel labelTry; // 시도 횟수 메시지
		JButton[] buttons = new JButton[16]; // 카드 버튼
		String[] images = { "image01.png", "image02.png", "image03.png", "image04.png", "image05.png", "image06.png",
				"image07.png", "image08.png", "image01.png", "image02.png", "image03.png", "image04.png", "image05.png",
				"image06.png", "image07.png", "image08.png" }; // 카드 이미지
		boolean[] openCheck = { false, false, false, false, false, false, false, false, false, false, false, false,
				false, false, false, false };
		int openCount = 0; // 카드 오픈 횟수 (0~2)
		int buttonIndexSave1 = 0; // 첫번째 오픈 카드 인덱스 (0~15)
		int buttonIndexSave2 = 0; // 두번째 오픈 카드 인덱스 (0~15)
		Timer timer; // 대기 시간용 타이머
		int tryCount = 0; // 시도 횟수
		int successCount = 0; // 성공한 횟수 (0~8)
		int score = 0; // 현재 점수
		int chainBonus = 0; // 연속으로 맞췄는지의 여부

		public MyFrame(String title) {
			// 레이아웃 설정
			super(title);
			this.setLayout(new BorderLayout());
			this.setSize(400, 500);
			this.setLocation(50, 50);
			this.setVisible(true);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			initUI(this); // UI 설정
			mixCard(); // 카드 순서를 섞는 함수
			for (int i = 0; i < 16; i++) {
				openCheck[i] = false;
			}
			playBGM("bgm.wav");

			this.pack();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (openCount == 2) {
				return;
			}

			JButton btn = (JButton) e.getSource();
			int index = getButtonIndex(btn);
			btn.setIcon(changeImage(images[index]));

			if (!openCheck[index]) {
				openCount++;
				if (openCount == 1) {
					buttonIndexSave1 = index;
				} else if (openCount == 2) {
					buttonIndexSave2 = index;
					if (buttonIndexSave1 != buttonIndexSave2) {
						tryCount++;
						labelTry.setText(" 시도 횟수: " + tryCount);

						boolean isBingo = checkCard(buttonIndexSave1, buttonIndexSave2);
						if (isBingo) {
							playSound("bingo.wav");
							openCount = 0;
							successCount++;
							chainBonus++;
							score += (100 * chainBonus);
							labelScore.setText(" Score: " + score);
							openCheck[buttonIndexSave1] = true;
							openCheck[buttonIndexSave2] = true;
							if (successCount == 8) {
								if (tryCount <= 8) {
									score += 2000;
									labelMessage.setText("퍼펙트 클리어!");
								} else {
									score += 1000;
									labelMessage.setText("게임 클리어!");
								}
								labelScore.setText(" Score: " + score);
							}
						} else {
							playSound("fail.wav");
							score -= 10;
							labelScore.setText(" Score: " + score);
							backToQuestion();
						}
					} else {
						openCount--;
					}
				}
			}
		}

		public int getButtonIndex(JButton btn) {
			int index = 0;
			for (int i = 0; i < 16; i++) {
				if (buttons[i] == btn) {
					index = i;
				}
			}
			return index;
		}

		public void initUI(MyFrame myFrame) {
			panelNorth = new JPanel();
			panelNorth.setPreferredSize(new Dimension(400, 50));
			panelNorth.setBackground(Color.RED);
			labelMessage = new JLabel("똑같은 포켓몬을 찾아라!");
			labelMessage.setPreferredSize(new Dimension(400, 40));
			labelMessage.setForeground(Color.BLACK);
			labelMessage.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
			labelMessage.setHorizontalAlignment(JLabel.CENTER);
			panelNorth.add(labelMessage);
			myFrame.add("North", panelNorth);

			panelWest = new JPanel();
			panelWest.setPreferredSize(new Dimension(200, 50));
			panelWest.setBackground(Color.WHITE);
			labelScore = new JLabel(" Score: " + score);
			labelScore.setPreferredSize(new Dimension(200, 40));
			labelScore.setForeground(Color.BLACK);
			labelScore.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
			panelWest.add(labelScore);
			myFrame.add("West", panelWest);

			panelCenter = new JPanel();
			panelCenter.setPreferredSize(new Dimension(200, 50));
			panelCenter.setBackground(Color.WHITE);
			labelTry = new JLabel(" 시도 횟수: " + tryCount);
			labelTry.setPreferredSize(new Dimension(200, 40));
			labelTry.setForeground(Color.BLACK);
			labelTry.setFont(new Font("HoonWhitecatR", Font.BOLD, 20));
			panelCenter.add(labelTry);
			myFrame.add("Center", panelCenter);

			panelSouth = new JPanel();
			panelSouth.setLayout(new GridLayout(4, 4));
			panelSouth.setPreferredSize(new Dimension(400, 400));
			for (int i = 0; i < 16; i++) {
				buttons[i] = new JButton();
				buttons[i].setPreferredSize(new Dimension(100, 100));
				buttons[i].setIcon(changeImage("card.png"));
				buttons[i].addActionListener(myFrame);
				panelSouth.add(buttons[i]);
			}
			myFrame.add("South", panelSouth);
		}

		public ImageIcon changeImage(String filename) {
			ImageIcon icon = new ImageIcon("./Image/" + filename);
			Image originImage = icon.getImage();
			Image changedImage = originImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH); // 크기 조정
			ImageIcon iconNew = new ImageIcon(changedImage);
			return iconNew;
		}

		public void mixCard() {
			Random rand = new Random();
			for (int i = 0; i < 1000; i++) {
				int random = rand.nextInt(15) + 1; // 1~15
				String temp = images[0];
				images[0] = images[random];
				images[random] = temp;
			}
		}

		public boolean checkCard(int index1, int index2) {
			if (index1 == index2) {
				return false;
			}
			if (images[index1].equals(images[index2])) {
				return true;
			} else {
				return false;
			}
		}

		public void backToQuestion() {
			timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openCount = 0;
					chainBonus = 0;
					buttons[buttonIndexSave1].setIcon(changeImage("card.png"));
					buttons[buttonIndexSave2].setIcon(changeImage("card.png"));
					timer.stop();
				}
			});
			timer.start();
		}

		public void playSound(String filename) {
			File file = new File("./Sound/" + filename);
			if (file.exists()) {
				try {
					AudioInputStream stream = AudioSystem.getAudioInputStream(file);
					Clip clip = AudioSystem.getClip();
					clip.open(stream);
					clip.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("File Not Found!");
			}
		}

		public void playBGM(String filename) {
			File file = new File("./Sound/" + filename);
			if (file.exists()) {
				try {
					AudioInputStream stream = AudioSystem.getAudioInputStream(file);
					Clip clip = AudioSystem.getClip();
					clip.open(stream);
					clip.start();
					clip.loop(10);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("File Not Found!");
			}
		}
	}

	static class MainFrame extends Frame implements MouseListener {
		Label lbl;

		MainFrame() {
			super("창 전환 1번 프레임");
			this.setVisible(true);
			this.setBounds(700, 400, 300, 300);
			this.setLayout(null);
			this.addWindowListener(new MyWinExit());
			lbl = new Label("다음 (클릭) ▶▶▶", 1);
			lbl.setBackground(Color.yellow);
			lbl.setBounds(70, 120, 130, 20);
			add(lbl);
			lbl.addMouseListener(this);
		}

		// paint() 메소드 구현
		public void paint(Graphics g) {
			g.drawString("여기는 첫 번째 프레임 11111", 70, 100);
		}

		// 윈도우 종료 클래스
		public class MyWinExit extends WindowAdapter {
			public void windowClosing(WindowEvent we) {
				System.exit(0); // 윈도 종료
			}
		}

		@Override // 마우스 이벤트 인터페이스 구현
		public void mouseClicked(MouseEvent e) {
			new MyFrame("Bingo!"); // 여기가 프레임 전환 역할
			this.setVisible(false);
		}

		@Override // 마우스 이벤트 인터페이스 구현
		public void mousePressed(MouseEvent e) {
			/* 구현생략 */}

		@Override // 마우스 이벤트 인터페이스 구현
		public void mouseReleased(MouseEvent e) {
			/* 구현생략 */}

		@Override // 마우스 이벤트 인터페이스 구현
		public void mouseEntered(MouseEvent e) {
			/* 구현생략 */}

		@Override // 마우스 이벤트 인터페이스 구현
		public void mouseExited(MouseEvent e) {
			/* 구현생략 */}
	}// 첫 번째 프레임 끝

	public static void main(String[] args) {
		// new MyFrame("Bingo Game");
		new MainFrame();
	}
}
