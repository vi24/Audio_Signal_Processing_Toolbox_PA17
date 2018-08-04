function beat = beatDetection(songFile)

%  if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\Maroon5_Sugar_drum_Cover_trimmed.mp3'; end
    if nargin < 1, songFile = 'C:\Users\Matthias\StudioProjects\Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_60bpm_1-4time_61beats_stereo.mp3'; end

    audioArray = audioread(songFile);
    info = audioinfo(songFile);
    packetLength = 1024;
    C = 22;
    amountOfPackets = floor(info.TotalSamples / packetLength);

    instantEnergies = zeros(amountOfPackets, 1);
    for i = 1 : amountOfPackets
        
        startIndex = (i-1)*packetLength +1;
        stopIndex = i*packetLength + 1;
        instantEnergies(i) = computeInstantEnergy(audioArray(startIndex:stopIndex));
        
    end
    x = [1:1:amountOfPackets];
    localEnergy = computeLocalEnergy(instantEnergies, packetLength);
   %C = abs((-0.0025614*computeVariance(instantEnergies,localEnergy,amountOfPackets) + 1.5142857));
    
    totalLargerThanlocalEnergy = 0;
    
    
    for i = 1 : amountOfPackets
        if(instantEnergies(i) > C*localEnergy )    
            totalLargerThanlocalEnergy = totalLargerThanlocalEnergy + 1;

        end
    end
    
    
    
    plot(x,instantEnergies);

    
    beat = (60/(info.TotalSamples/info.SampleRate))*totalLargerThanlocalEnergy;

end


function e = computeInstantEnergy(samples)

sum = 0;

for i = 1: length(samples)
    
    
    sum = sum + samples(i)^2;
end

e = sum;
end

function E = computeLocalEnergy(instantEnergies, packetSize )

sum = 0;
factor = packetSize/(packetSize*length(instantEnergies));


for i = 1 : length(instantEnergies)

    sum = sum + instantEnergies(i);
end

E = factor *sum;

end



function V = computeVariance(instantEnergies, localEnergy, amountOfPackets)

factor = 1/amountOfPackets;
sum = 0 ;

for i = 1 : amountOfPackets
    
    sum = sum + (instantEnergies(i) - localEnergy)^2;
    
end



V = sum;

end
