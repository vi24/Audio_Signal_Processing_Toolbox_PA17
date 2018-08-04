 [parentdir,~,~]=fileparts(pwd);
file = strcat(parentdir,'\documents\audioFiles\TeddybearsSthlm_HeyBoy_trimmedToOneMinute.mp3');
file

[y,Fs] = audioread(file);
info = audioinfo(file);
%[y,Fs] = audioread('test.mp3');
%info = audioinfo('test.mp3')

fs = info.SampleRate;
% fs/FFT_Length = resolution
FFT_Length = 64;
resolution = fs/FFT_Length;
Ts = 1/fs;
dt = (0:Ts:size(y,1)*Ts)';
dt = dt(1:end-1,:);
fn = [0:1.0/FFT_Length:1-1.0/FFT_Length];


signalFFT = fft(y(:,1));
spectralMagnitudeDB = 20*log10(abs(signalFFT/FFT_Length)); 



plot(dt,y(:,1));
xlim([0 60]);
title('Time domain')
xlabel('Time [s]');
ylabel('relative amplitude ')

% 
% figure();
% semilogx(spectralMagnitudeDB);
% title('Frequency domain')
% xlabel('Frequency [Hz]');
% ylabel('Amplitude')


