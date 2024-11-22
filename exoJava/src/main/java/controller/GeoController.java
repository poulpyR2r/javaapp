package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import com.github.sarxos.webcam.Webcam;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeoController {

    @FXML
    private Label locationLabel;

    @FXML
    private Button getLocationButton;
    @FXML
    private Button takePhotoButton;
    @FXML
    private Button startRecordingButton;
    @FXML
    private Button stopRecordingButton;
    @FXML
    private Button recordKeyboardButton;
     @FXML
    private Pane rootPane; 

    private TargetDataLine audioLine;
    private boolean isRecordingKeyboard = false;
    private StringBuilder keyboardInput = new StringBuilder();

    @FXML
    public void initialize() {
        getLocationButton.setOnAction(event -> getLocation());
        takePhotoButton.setOnAction(event -> takePhoto());
        startRecordingButton.setOnAction(event -> startRecording());
        stopRecordingButton.setOnAction(event -> stopRecording());
        recordKeyboardButton.setOnAction(event -> toggleKeyboardRecording());

        rootPane.setOnKeyTyped(this::handleKeyTyped);
    }

    private void getLocation() {
        try {
            String apiUrl = "http://ip-api.com/json/";
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String json = response.toString();
            double latitude = Double.parseDouble(json.split("\"lat\":")[1].split(",")[0]);
            double longitude = Double.parseDouble(json.split("\"lon\":")[1].split(",")[0]);

            locationLabel
                    .setText(String.format("Current Location: Latitude %.4f, Longitude %.4f", latitude, longitude));
        } catch (Exception e) {
            locationLabel.setText("Error fetching location: " + e.getMessage());
        }
    }

    private void toggleKeyboardRecording() {
        isRecordingKeyboard = !isRecordingKeyboard; 
        if (isRecordingKeyboard) {
            keyboardInput.setLength(0); 
            locationLabel.setText("Keyboard recording started...");
        } else {
            locationLabel.setText("Keyboard recording stopped. Captured: " + keyboardInput.toString());
        }
    }

    private void handleKeyTyped(KeyEvent event) {
        if (isRecordingKeyboard) {
            keyboardInput.append(event.getCharacter()); 
        }
    }

    private void takePhoto() {
        Webcam webcam = Webcam.getDefault(); 
        if (webcam != null) {
            webcam.open();
            try {
                File photoFile = new File("photo.png");
                ImageIO.write(webcam.getImage(), "PNG", photoFile);
                locationLabel.setText("Photo saved at: " + photoFile.getAbsolutePath());
            } catch (IOException e) {
                locationLabel.setText("Error saving photo: " + e.getMessage());
            } finally {
                webcam.close();
            }
        } else {
            locationLabel.setText("No webcam detected!");
        }
    }

    private void startRecording() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                locationLabel.setText("Audio line not supported!");
                return;
            }

            audioLine = (TargetDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();

            Thread recordingThread = new Thread(() -> {
                try (AudioInputStream audioStream = new AudioInputStream(audioLine)) {
                    File audioFile = new File("audio.wav");
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
                } catch (IOException e) {
                    locationLabel.setText("Error saving audio: " + e.getMessage());
                }
            });

            recordingThread.start();
            locationLabel.setText("Recording started...");
        } catch (LineUnavailableException e) {
            locationLabel.setText("Error starting recording: " + e.getMessage());
        }
    }

    private void stopRecording() {
        if (audioLine != null && audioLine.isRunning()) {
            audioLine.stop();
            audioLine.close();
            locationLabel.setText("Recording stopped. Audio saved as 'audio.wav'");
        } else {
            locationLabel.setText("No recording in progress.");
        }
    }
}
