let mediaRecorder;
let audioChunks = [];

const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const status = document.getElementById("status");
const transcription = document.getElementById("transcription");

startButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);

async function startRecording() {

    try {

        const stream = await navigator.mediaDevices.getUserMedia({
            audio: true
        });

        audioChunks = [];

        mediaRecorder = new MediaRecorder(stream);

        mediaRecorder.addEventListener("dataavailable", event => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        });

        mediaRecorder.addEventListener("stop", async () => {

            const audioBlob = new Blob(audioChunks, {
                type: mediaRecorder.mimeType
            });

            stream.getTracks().forEach(track => track.stop());

            await uploadAudio(audioBlob);
        });

        mediaRecorder.start();

        startButton.disabled = true;
        stopButton.disabled = false;

        status.textContent = "Recording... Speak now.";

    } catch (error) {

        console.error(error);

        status.textContent =
            "Unable to access microphone. Please check your browser permissions.";
    }
}

function stopRecording() {

    if (mediaRecorder && mediaRecorder.state !== "inactive") {

        mediaRecorder.stop();

        startButton.disabled = false;
        stopButton.disabled = true;

        status.textContent = "Uploading audio...";
    }
}

async function uploadAudio(audioBlob) {

    try {

        const formData = new FormData();

        formData.append("audio", audioBlob, "recording.webm");

        const response = await fetch("/api/v1/transcribe", {
            method: "POST",
            body: formData
        });


		if (!response.ok) {
		    const errorText = await response.text();
		    throw new Error(
		        "Server returned " + response.status + ": " + errorText
		    );
		}

        const data = await response.json();

        transcription.textContent = data.text;

        status.textContent = "Ready to record again.";

		} catch (error) {
		    console.error(error);
		    status.textContent = error.message;
		}
}