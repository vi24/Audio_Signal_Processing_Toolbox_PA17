function beat = beatDetection(songFile)
 %
 if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_60bpm_1-4time_61beats_stereo.mp3'; end
  timeWindowsInMs = 1;

 
 audioArray = audioread(songFile);
 info = audioinfo(songFile);
 sampleRate = info.SampleRate;
 timeStep = 1/sampleRate;
 samplesPerEnergy = sampleRate/(1000/timeWindowsInMs);
 instantEnergyArray = zeros(floor(length(audioArray)/samplesPerEnergy)+1,1);
 
 dt = (0:timeStep:size(audioArray,1)*timeStep)';
dt = dt(1:end-1,:);
 
 for i = 1 : length(instantEnergyArray)-1
 
     instantEnergyArray(i) = calculateInstantenousEnergy(audioArray((i-1)*samplesPerEnergy+1:i*samplesPerEnergy ));
     
 end
      instantEnergyArray(end) = calculateInstantenousEnergy(audioArray((length(instantEnergyArray)-1)*samplesPerEnergy:end));


 
  n = 2^nextpow2(length(audioArray));
  Y = fft(instantEnergyArray,n,1);
  P2 = Y/n;
 P1 = P2(1:n/2+1);
 P1(:,2:end-1) = 2*P1(:,2:end-1);
  dtNEW = (0:timeStep:length(P2)*timeStep)';
dtNEW = dtNEW(1:end-1,:);

%  plot(dt,audioArray);
%  figure();
%  plot(instantEnergyArray);
 
% figure();
% plot((0:(sampleRate/n):(sampleRate-sampleRate/n))',P2)

% figure();
% plot(0:(sampleRate/n):(sampleRate/2),P1(1:n/2));

[maxValue, index] = max(P1);
dtNEW(index);
 beat = dtNEW(index)*60 ;
end


function instantenousEnergy = calculateInstantenousEnergy(samples)

size = length(samples);
sum = 0;
for i = 1 : size
    sum = sum + samples(i)^2;
end

instantenousEnergy = sum;
end




