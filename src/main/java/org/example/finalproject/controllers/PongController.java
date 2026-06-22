package org.example.finalproject.controllers;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import org.example.finalproject.MySQL;
import org.example.finalproject.User;
import org.example.finalproject.Utils;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;

public class PongController implements Initializable {

    @FXML private AnchorPane paddleSquarePane;

    // B - blue | R - red
    @FXML private Label scoreBLabel;
    @FXML private Label scoreRLabel;
    private int scoreB = 0;
    private int scoreR = 0;

    @FXML private Rectangle paddleB;
    @FXML private Rectangle paddleR;
    @FXML private Circle ball;

    private static int paddleBWidth;
    private static int paddleBHeigth;
    private static int paddleRWidth;
    private static int paddleRHeigth;
    private static int ballRadius;
    private static double paddleBSpeed;
    private static double paddleRSpeed;
    private static double[] ballSpeed = new double[2]; // {X, Y}
    private static int reachedBorderX;

    @FXML private Button gameStartButton;
    @FXML private Label hintTitleLabel;
    @FXML private Label hintBLabel;
    @FXML private Label hintRLabel;
    @FXML private Label hintScoreLabel;

    private Set<KeyCode> keyCodeSet = new HashSet<>();
    private AnimationTimer animationTimer;

    @FXML
    private void handleGameStart() {
        gameStartButton.setManaged(false); gameStartButton.setVisible(false);
        hintTitleLabel.setVisible(false); hintTitleLabel.setManaged(false);
        hintBLabel.setVisible(false); hintBLabel.setManaged(false);
        hintRLabel.setVisible(false); hintRLabel.setVisible(false);
        hintScoreLabel.setVisible(false); hintScoreLabel.setVisible(false);
        paddleSquarePane.setFocusTraversable(true); paddleSquarePane.requestFocus();
        //

        Random random = new Random();
        int redPaddleDirection = random.nextInt(0,2);

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
//                double topBorder = (int) -((paddleSquarePane.getPrefHeight() / 2) - (paddleB.getHeight() / 2) - 4);
                double topBorder = 0;
                double bottomBorderB = paddleSquarePane.getPrefHeight() - paddleB.getHeight();
                double bottomBorderR = paddleSquarePane.getPrefHeight() - paddleR.getHeight();
                double paddleRCenter = paddleR.getY() + paddleR.getHeight() / 2;
                double[] ballCenters = { ball.getCenterX(), ball.getCenterY() };

                if(keyCodeSet.contains(KeyCode.W)) {
                    paddleB.setLayoutY(findMax(paddleB.getLayoutY() - paddleBSpeed, topBorder, bottomBorderB));
                }
                if(keyCodeSet.contains(KeyCode.S)) {
                    paddleB.setLayoutY(findMax(paddleB.getLayoutY() + paddleBSpeed, topBorder, bottomBorderB));
                }
//                System.out.println(paddleR.getY() + " || " + topBorder + " | " + bottomBorder);


                if(reachedBorderX == 0) {
                    double newY = redPaddleDirection == 0 ? paddleR.getLayoutY() - paddleRSpeed : paddleR.getLayoutY() + paddleRSpeed;
                    paddleR.setLayoutY(findMax(newY, topBorder, bottomBorderR));
                }

                if(paddleR.getLayoutY() <= topBorder) {
                    reachedBorderX = 1;
                }
                if(paddleR.getLayoutY() >= bottomBorderR) {
                    reachedBorderX = 2;
                }

                if(reachedBorderX == 1) {
                    paddleR.setLayoutY(findMax(paddleR.getLayoutY() + paddleRSpeed, topBorder, bottomBorderR));
                } else if(reachedBorderX == 2) {
                    paddleR.setLayoutY(findMax(paddleR.getLayoutY() - paddleRSpeed, topBorder, bottomBorderR));
                }



                if(ball.getLayoutX() + ball.getRadius() >= (paddleSquarePane.getWidth()-1)){
                    ball.setLayoutX(paddleSquarePane.getPrefWidth()/2);
                    ball.setLayoutY(paddleSquarePane.getPrefHeight()/2);
                    updateScore("blue");
                }

                if(ball.getLayoutX() - ball.getRadius() <= 1){
                    ball.setLayoutX(paddleSquarePane.getPrefWidth()/2);
                    ball.setLayoutY(paddleSquarePane.getPrefHeight()/2);
                    updateScore("red");
                }

                if(ball.getLayoutY() + ball.getRadius() >= paddleSquarePane.getPrefHeight()-1 || ball.getLayoutY()-ball.getRadius() <= 1)
                    ballSpeed[1] = -ballSpeed[1];


                boolean hitPlayer1 = ball.getBoundsInParent().intersects(paddleB.getBoundsInParent());
                boolean hitPlayer2 = ball.getBoundsInParent().intersects(paddleR.getBoundsInParent());

                if(hitPlayer1 && ballSpeed[0] < 0) {
                    ballSpeed[0] = -ballSpeed[0];
                    ball.setLayoutX(paddleB.getLayoutX() + paddleB.getWidth() + ball.getRadius());
                }

                if(hitPlayer2 && ballSpeed[0] > 0) {
                    ballSpeed[0] = -ballSpeed[0];
                    ball.setLayoutX(paddleR.getLayoutX() - ball.getRadius());
                }

                ball.setLayoutX(ball.getLayoutX() + ballSpeed[0]);
                ball.setLayoutY(ball.getLayoutY() + ballSpeed[1]);

//                if(keyCodeSet.contains(KeyCode.UP) && paddleR.getY() > topBorder) {
//                    paddleR.setY(paddleR.getY() - paddleRSpeed);
//                }
//                if(keyCodeSet.contains(KeyCode.DOWN) && paddleR.getY() < bottomBorder) {
//                    paddleR.setY(paddleR.getY()+paddleRSpeed);
//                }
            }
        };
        animationTimer.start();

    }

    private void configureGameProperties() {
        if(paddleSquarePane == null){
            System.out.println("Critical: Paddle Square Pane Not Fond!!!");
            return;
        }

        paddleBWidth = 15;
        paddleRWidth = 15;
        paddleBHeigth = 120;
        paddleRHeigth = 90;
        ballRadius = 22;
        paddleBSpeed = 3;
        paddleRSpeed = 2.6;
        ballSpeed[0] = 2.2; // X
        ballSpeed[1] = 2.2; // Y

        //
        paddleB.setWidth(paddleBWidth);
        paddleB.setHeight(paddleBHeigth);
        paddleR.setWidth(paddleRWidth);
        paddleR.setHeight(paddleRHeigth);
        ball.setRadius(ballRadius);

        paddleB.setLayoutY((paddleSquarePane.getPrefHeight() - paddleBHeigth) / 2);
        paddleR.setLayoutY((paddleSquarePane.getPrefHeight() - paddleRHeigth) / 2);

        // --
        scoreBLabel.setText("" + scoreB);
        scoreRLabel.setText("" + scoreR);

    }

    private void updateScore(String side) {
        if(side.equals("blue")) {
            scoreB++;
            scoreBLabel.setText(""+scoreB);

            switch(scoreB) {
                case 2: {
                    paddleBHeigth -= 10;
                    paddleBSpeed -= 0.3;

                    paddleRSpeed += 0.25;
                    break;
                } case 4: {
                    paddleBHeigth -= 15;
                    paddleBSpeed -= 0.3;

                    paddleRHeigth += 10;
                    paddleRSpeed += 0.25;

                    ballRadius -= 2;
                    ballSpeed[0] += 0.4;
                    ballSpeed[1] += 0.4;
                    break;
                } case 6: {
                    paddleBHeigth -= 10;
                    paddleBSpeed -= 0.15;

                    paddleRHeigth += 10;
                    paddleRSpeed += 0.3;

                    ballRadius -= 3;
                    ballSpeed[0] += 0.55;
                    ballSpeed[1] += 0.55;
                    break;
                }
            }
        }
        if(scoreB%2 == 0) {
            setPaddleHeight(paddleB, paddleBHeigth);
            setPaddleHeight(paddleR, paddleRHeigth);
            ball.setRadius(ballRadius);
        }
    }

    private double findMax(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void setPaddleHeight(Rectangle paddle, double newHeight) {
        double oldCenterY = paddle.getLayoutY() + paddle.getHeight() / 2;

        paddle.setHeight(newHeight);

        double newY = oldCenterY - newHeight / 2;
        double maxY = paddleSquarePane.getPrefHeight() - newHeight;

        paddle.setLayoutY(findMax(newY, 0, maxY));
    }

    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        if(animationTimer != null) animationTimer.stop();
        if(scoreB >= 0) {
            Thread thread = new Thread(() -> {
                try {
                    PreparedStatement preparedStatement = MySQL.connection.prepareStatement("UPDATE users SET totalPongScores = ? WHERE id = ?");
                    preparedStatement.setInt(1, (User.currentUser.getTotalPongScores() + scoreB));
                    preparedStatement.setInt(2, User.currentUser.getID());
                    int rows = preparedStatement.executeUpdate();
                    System.out.println(rows);
                } catch (SQLException e) {
                    System.out.println("Pong: Failed to update game score");
                    System.out.println(e.getMessage());
                }
            });
            thread.start();
        }
        Utils.changeScene(event, "games-page");

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configureGameProperties();

        Platform.runLater(() -> {
                paddleSquarePane.getScene().setOnKeyPressed(e -> {
                    keyCodeSet.add(e.getCode());
//                        System.out.println(e.getCode());
                });
                paddleSquarePane.getScene().setOnKeyReleased(e -> {
                    keyCodeSet.remove(e.getCode());
                });
            }
        );
    }

}
