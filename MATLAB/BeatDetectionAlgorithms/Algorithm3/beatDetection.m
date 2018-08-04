function [bpm] = beatDetection(file)

[audio, fs] = audioread(file);
bpms = tempo(audio(:,1), fs);

bpm = bpms(2);



end



