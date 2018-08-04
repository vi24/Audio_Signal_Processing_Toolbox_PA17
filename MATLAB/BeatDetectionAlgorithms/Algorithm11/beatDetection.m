function beat = beatDetection(songFile)

 if nargin < 1, songFile = 'C:\Users\Matthias.Kaderli\Documents\GitHub\PAIT17_Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_60bpm_1-4time_61beats_stereo.mp3'; end
%    if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_20bpm_1-4time_21beats_stereo.mp3'; end
%if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\TwoStepsFromHell_RunLikeHell.mp3'; end

[audioArray, sampleRate ] = audioread(songFile);
info = audioinfo(songFile);

windowsInstantEnergyMs = 20;
CountOfInstantEnergyForSteadyEnergy = 100;
alpha = 0.0025;
beta = 1.5;
samplesPerWindow = sampleRate * (windowsInstantEnergyMs/1000);

instantEnergies = zeros(CountOfInstantEnergyForSteadyEnergy,1);

for i = 1 : CountOfInstantEnergyForSteadyEnergy
    
    startIndex = (i-1)*samplesPerWindow + 1;
    stopIndex = i*samplesPerWindow;
    instantEnergies(i) = calcInstantEnergy(audioArray(startIndex:stopIndex));
end

steadyLocalEnergy = mean(instantEnergies);
variance = calcVariance(instantEnergies,steadyLocalEnergy);
threshold = beta -alpha * variance;

amountAboveThreshold = 0;

for i = 1 : length(instantEnergies)
if(instantEnergies(i) > threshold)
    amountAboveThreshold = amountAboveThreshold + 1;
end

end

beat = (amountAboveThreshold / 2.0) * 60;

end

function instant = calcInstantEnergy(samples)

sum = 0;

for i = 1: length(samples)
    sum = sum + samples(i)^2;
end

instant = sum;
end

function variance = calcVariance(instantEnergies, steadyLocalEnergy)

sum = 0 ; 
for i = 1 : length(instantEnergies)
    sum = sum + (instantEnergies(i)- steadyLocalEnergy)^2;
end
variance = (1/length(instantEnergies))*sum;

end





