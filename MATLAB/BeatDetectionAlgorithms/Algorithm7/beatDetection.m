function beat = beatDetection(songFile)

 if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\Maroon5_Sugar_drum_Cover_trimmed.mp3'; end
%    if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_20bpm_1-4time_21beats_stereo.mp3'; end
%if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\TwoStepsFromHell_RunLikeHell.mp3'; end

[audioAsArray, fs] = audioread(songFile);
info = audioinfo(songFile);
timeStep = 1/fs;
dt = (0:timeStep:size(audioAsArray,1)*timeStep)';
dt = dt(1:end-1,:);

absoluteAudio = abs(audioAsArray);
n = 2^nextpow2(length(absoluteAudio));
Y = fft(absoluteAudio,n,1);
P2 = (Y/n);
P2abs = abs(P2);
P1 = P2(1:n/2+1);
P1(:,2:end-1) = 2*P1(:,2:end-1);
bandFilteredFFT = applyBandPassFilter(0:(fs/n):(fs/2-fs/n), P1(1:n/2), 0,1500);
bandFilteredTimeDomain = abs(ifft(bandFilteredFFT*2*n,n,1));
bandFilteredPeaks = valuesAboveThreshold(bandFilteredTimeDomain, 0.90);
peaks = calculatePeakSamples(bandFilteredPeaks);
[offset distance] = calculateDistanceBetweenPeaks(peaks);


dtNEW = (0:timeStep:length(P2)*timeStep)';
dtNEW = dtNEW(1:end-1,:);


subplot(3,3,1)

plot(dt, absoluteAudio);
xlim([0 10]);
title('Time domain of audio File')

%plot(dt, thresholdArray);
%xlim([0 10]);
%title('Time domain peaks of audio File')
%figure();

subplot(3,3,2)
x = [0:(fs/n):(fs-fs/n)]';
plot((0:(fs/n):(fs-fs/n))',P2)
title('P2')

subplot(3,3,3)

plot((0:(fs/n):(fs-fs/n))',P2abs)
title('P2 absolut')

subplot(3,3,4)

plot(0:(fs/n):(fs/2-fs/n),P1(1:n/2))
title('P1 fft of audio File')

subplot(3,3,5)
plot(0:(fs/n):(fs/2-fs/n),bandFilteredFFT)
title('bandfiltered P1 fft of peaks of audio File')

subplot(3,3,6)
plot(dtNEW,bandFilteredTimeDomain)
title('bandfiltered P1 timeDomain using iFFT')
ylim([0 1]);
xlim([0 10]);

subplot(3,3,7)
plot(dtNEW,bandFilteredPeaks);
title('bandfiltered peaks after ifft')
ylim([0 1]);
xlim([0 10]);

subplot(3,3,8)
plot(distance);
title('distance of peaks')
ylabel('distance of frames between peaks');
xlabel('occurrences')


beat = calculateMostLikelyBeat(distance,fs);

end


function values = valuesAboveThreshold(audioArray, threshold)
originalLength = length(audioArray);
thresholdArray = zeros(originalLength(),1);
i = 1;
while (i <= originalLength)
    if(audioArray(i) >= threshold)
        % fprintf('%i found one above threshold\n', i);
        thresholdArray(i) = audioArray(i);
        i = i + 5000;
    else
        thresholdArray(i) = 0;
        i = i +1;
    end
end

values =  thresholdArray;
end

function values = applyBandPassFilter(fftXArray, fftYArray,minFrequency , maxFrequency)
bandFilteredArray = zeros(length(fftYArray),1);
minIndex = 0;
maxIndex = 0;

for i = 1: length(fftXArray)
    if(fftXArray(i) > minFrequency)
        minIndex = i;
        break;
    end
end

for i = minIndex : length(fftXArray)
    if(fftXArray(i) > maxFrequency)
        maxIndex = i;
        break;
    end
    
end

for i = minIndex : maxIndex-1
    bandFilteredArray(i) = fftYArray(i);
end

values = bandFilteredArray;

end

function peaks = calculatePeakSamples(sampleArray)
peakArray = zeros();
counter = 1;
for i = 1 : length(sampleArray)
    if(sampleArray(i) ~= 0)
        peakArray(counter) = i;
        counter = counter + 1;
    end
    
    
end

peaks = peakArray;

end

function [offset, distance] =  calculateDistanceBetweenPeaks(peakSampleArray )
distanceArray = zeros();

for i = 2: length(peakSampleArray)
    distanceArray(i-1) = round(peakSampleArray(i)-peakSampleArray(i-1),-1);
end

offset = peakSampleArray(1);
distance = distanceArray;
end

function beat =  calculateMostLikelyBeat(distanceArray,fs)
beats = zeros(1000,1);

for i = 1 : length(distanceArray)
    %% TODO calculation may be wrong
    tempBeat = round(fs/distanceArray(i)*60);
    beats(tempBeat) = beats(tempBeat) + 1/length(distanceArray);
end
[maxValue, index] = max(beats);

beat =  index;

end



