function beat = beatDetection(songFile)

 if nargin < 1, songFile = 'C:\Users\Matthias.Kaderli\Documents\GitHub\PAIT17_Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_60bpm_1-4time_61beats_stereo.mp3'; end
%    if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_20bpm_1-4time_21beats_stereo.mp3'; end
%if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\TwoStepsFromHell_RunLikeHell.mp3'; end

[audioArray, sampleRate ] = audioread(songFile);
info = audioinfo(songFile);

windowsInstantEnergyMs = 20;
CountOfInstantEnergyForSteadyEnergy = 100;
b = 0.1;
samplesPerWindow = sampleRate * (windowsInstantEnergyMs/1000);

instantEnergies = zeros(CountOfInstantEnergyForSteadyEnergy,1);

for i = 1 : CountOfInstantEnergyForSteadyEnergy
    
    startIndex = (i-1)*samplesPerWindow + 1;
    stopIndex = i*samplesPerWindow;
    instantEnergies(i) = calcInstantEnergy(audioArray(startIndex:stopIndex));
end



steadyLocalEnergy = mean(instantEnergies);




beats = 0;

for i = i : length(instantEnergies)
    
    if instantEnergies(i) > b * steadyLocalEnergy
    
        beats = beats + 1;
    end
    
    
end

beat = (beats/2.0) *60;
end

function instant = calcInstantEnergy(samples)

sum = 0;

for i = 1: length(samples)
    sum = sum + samples(i)^2;
end

instant = sum;
end



