function beat = testFFT()

JavatimeDomain = importdata("timeDomainSamplesAbs.txt");
JavafftSamples = importdata("fftSamples.txt");
JavabandFilteredFFTSamples = importdata("bandfilteredFFTSamples.txt");
JavabandFilteredIFFTSamples = importdata("bandfilteredifftSamples.txt");
JavaFilteredIFFTSamples = importdata("filteredIFFTSamples.txt");
JavaNormalizedIFFTSamples = importdata("normalizedIFFTSamples.txt");

audioAsArray = JavatimeDomain;

fs = 48000;

nAndroid = 2^nextpow2(length(JavatimeDomain));
%nAndroid = 4096;

timeStep = 1/fs;
dt = (0:timeStep:size(audioAsArray,1)*timeStep)';
dt = dt(1:end-1,:);

absoluteAudio = abs(audioAsArray);
%n = 2^20;
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
[offset, distance] = calculateDistanceBetweenPeaks(peaks);


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

displayJavaValues(fs,nAndroid, JavatimeDomain,JavafftSamples,JavabandFilteredFFTSamples,JavabandFilteredIFFTSamples, JavaNormalizedIFFTSamples,JavaFilteredIFFTSamples);



beat = calculateMostLikelyBeat(distance,fs);

end

function stuff = displayJavaValues(fs, nAndroid,JavatimeDomainAbs, JavafftSamples,JavabandFilteredFFTSamples,JavaIFFTSamples,JavaNormalizedIFFTSamples ,JavaValuefilteredIFFTSamples)
figure()
timeStep = 1/fs;
subplot(3,3,1);
dtJavaTimeDomain = [0: timeStep:length(JavatimeDomainAbs)/fs];
dtJavaTimeDomain = dtJavaTimeDomain(1:end-1);
plot(dtJavaTimeDomain, JavatimeDomainAbs);
title('Java time Domain')


subplot(3,3,2)
dtFFTSamples = (0:(fs/nAndroid):(fs/2-fs/nAndroid));
plot(dtFFTSamples,JavafftSamples(1:nAndroid/2));
title('Java fft')


subplot(3,3,3)
dtFFTSamples = (0:(fs/nAndroid):(fs/2-fs/nAndroid));
plot(dtFFTSamples,JavabandFilteredFFTSamples(1:nAndroid/2));
title('Java bandfiltered fft')


subplot(3,3,4)
new = JavaIFFTSamples(1:length(dtJavaTimeDomain));
plot(dtJavaTimeDomain, new);
title('Java bandfiltered ifft')


subplot(3,3,5)
normalized = JavaNormalizedIFFTSamples(1:length(dtJavaTimeDomain));
plot(dtJavaTimeDomain,normalized );
title('Java normalized ifft Time Domain')

subplot(3,3,6)
filtered = JavaValuefilteredIFFTSamples(1:length(dtJavaTimeDomain));
plot(dtJavaTimeDomain,filtered );
title('Java valuefiltered ifft Time Domain')






stuff = -1;

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

for i = 1 : length(beats)

    if(beats(i) ~= 0)
        fprintf('BPM: %d with certainty: %f\n', i, beats(i))
    end
end


[maxValue, index] = max(beats);

beat =  index;

end







% 
% fs = 48000
% 
% originalTimeDomain =  audioread('C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_60bpm_1-4time_61beats_stereo.mp3');
% nOriginal = 2^nextpow2(length(originalTimeDomain));
% originalP2FFT = (fft(abs(originalTimeDomain),nOriginal,1))/nOriginal;
% originalP2AbsFFT = abs(originalP2FFT);
% originalP1FFT = originalP2AbsFFT(1:nOriginal/2+1);
% originalP1FFT(:,2:end-1) = 2*originalP1FFT(:,2:end-1);
% 
% 
% JavatimeDomain = importdata("timeDomainSamples.txt");
% JavafftSamples = importdata("fftSamples.txt");
% nAndroid = 2^nextpow2(length(JavatimeDomain));
% JavaIFFTSamples = importdata("bandfilteredifftSamples.txt");
% JavabandFilteredFFTSamples = importdata("bandfilteredFFTSamples.txt");
% 
% JavaTimeDomainProcesses = zeros(floor(length(JavatimeDomain)/2),1);
% 
% 
% 
% for i=1: floor(length(JavatimeDomain)/2)
%     JavaTimeDomainProcesses(i) = JavatimeDomain(i*2);
% end
% JavaTimeDomainProcesses(i) = abs(JavaTimeDomainProcesses(i));
% 
% nJavaProc = 2^nextpow2(length(JavaTimeDomainProcesses));
% JavaP2FFT = (fft(abs(JavaTimeDomainProcesses),nJavaProc,1))/nJavaProc;
% JavaP2AbsFFT = abs(JavaP2FFT);
% JavaP1FFT = JavaP2AbsFFT(1:nJavaProc/2+1);
% JavaP1FFT(:,2:end-1) = 2*JavaP1FFT(:,2:end-1);
% JavaP1FFT = 2*JavaP1FFT;
% 




% 
% % 
% % dtOriginal = [0:60 *1/length(originalTimeDomain):60];
% % dtOriginal = dtOriginal(1:end-1);
% % plot(dtOriginal, originalTimeDomain);
% % title('Matlab time Domain');
% 
% figure();
% 
% dtJavaTimeDomain = [0:60*1/length(JavatimeDomain):60];
% dtJavaTimeDomain = dtJavaTimeDomain(1:end-1);
% plot(dtJavaTimeDomain, JavatimeDomain);
% title('Java time Domain')
% 
% figure;
% % 
% % dtOriginalP1FFT = (0:(fs/nOriginal):(fs/2-fs/nOriginal));
% % plot(dtOriginalP1FFT,originalP1FFT(1:nOriginal/2));
% % title('Matlab fft')
% % figure();
% 
% dtFFTSamples = (0:(fs/nAndroid):(fs/2-fs/nAndroid));
% plot(dtFFTSamples,JavafftSamples(1:nAndroid/2));
% title('Java fft')
% figure();
% 
% %  dtJavaP1FFT = (0:(fs/nJavaProc):(fs/2-fs/nJavaProc));
% %  plot(dtJavaP1FFT,JavaP1FFT(1:nJavaProc/2));
% %  title('processed Java fft')
% 
% 
% dtFFTSamples = (0:(fs/nAndroid):(fs/2-fs/nAndroid));
% plot(dtFFTSamples,JavabandFilteredFFTSamples(1:nAndroid/2));
% title('Java bandfiltered fft')
% 
% figure()
% timeStep = 1/fs
% dtJavaIFFT = [0:60/length(JavaIFFTSamples):60];
% dtJavaIFFT = dtJavaIFFT(1:end-1);
% new = JavaIFFTSamples(1:length(dtJavaTimeDomain));
% plot(dtJavaTimeDomain, new);
% title('Java bandfiltered ifft')
% 
% figure();
% 
% dtNEW = (0:timeStep:length(JavabandFilteredFFTSamples)*timeStep)';
% dtNEW = dtNEW(1:end-1);
% javaProcessedIFFT = abs(ifft(JavabandFilteredFFTSamples*2*nAndroid,nAndroid,1));
% plot(dtNEW,javaProcessedIFFT );
% %title('Java FFT, Matlab Processed IFFT')
% 
% 
% %fs = 48000;
% %n = 2^nextpow2(length(fftSamples));
% 
% %plot(0:(fs/n):(fs/2-fs/n),fftSamples(1:n/2))
% %title('P1 fft of Java')
% 
% 
% 
% 
