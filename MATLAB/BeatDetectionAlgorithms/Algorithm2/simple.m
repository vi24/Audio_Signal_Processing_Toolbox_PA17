
song = 'C:\Users\Ravi\Documents\GitHub\PAIT17_Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_240bpm_1-4time_241beats_stereo.mp3';
clear y Fs
[y,Fs] = audioread(song);
info = audioinfo(song)
samplesPerBlock = 1024;
beatCounter = 0;
blocksPerSecond = info.SampleRate/samplesPerBlock;
blockEnergy = zeros(1,round(info.TotalSamples/samplesPerBlock));
totalEnergy = 0;
blocksMax = round(size(y)/samplesPerBlock);
blocksCounter = 0;
sampleCounter = 1;

for i = 1 : blocksMax
    for j = 1 : samplesPerBlock
            if(sampleCounter <= size(y,1))
                for k = 1 : info.NumChannels
                    totalEnergy = totalEnergy + y(sampleCounter,k);
                end
                sampleCounter = sampleCounter + 1;
                if(sampleCounter > info.TotalSamples)
                    ME = MException("samples overload");
                    throw(ME);
                end
                if(sampleCounter == info.TotalSamples)
                    fprintf("Sample: " + sampleCounter + "block" + i);
                end
            end
    end
    if (i < 10)
        sampleCounter;
    end
    blockEnergy(i) = totalEnergy;
    totalEnergy = 0;
end

sum = 0;
for m = 1 : (length(blockEnergy)-1)
    sum = sum + mean(blockEnergy) - blockEnergy(m);
end

variance = (1/m) * sum;
C = -0.0000015 * variance + 1.5142857;


for i = 1 : m
    if(blockEnergy(i)> C*mean(blockEnergy))
        beatCounter = beatCounter + 1;
    end
end

disp((beatCounter/info.Duration)*60);

