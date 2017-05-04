function BoomChart(fname,t)
[folder,file,ext] = fileparts(fname);

fname = string(fname);
t = string(t);

fID = fopen(fname);
C = textscan(fID,'%u, %f, %f');
m1 = C{:,1};
m2 = C{:,2};
m3 = C{:,3};

plot(m1,m2);
xlabel('iterations');
ylabel('loss');
title(t);
pbaspect([4 1 1]);

ax = gca;
outerpos = ax.OuterPosition;
ti = ax.TightInset; 
left = outerpos(1) + ti(1);
bottom = outerpos(2) + ti(2);
ax_width = outerpos(3) - ti(1) - ti(3);
ax_height = outerpos(4) - ti(2) - ti(4);
ax.Position = [left bottom ax_width ax_height];

saveas(gcf,strcat(folder,'\',file,'_','loss'),'eps');

clf('reset')

plot(m1,m3);
xlabel('iterations');
ylabel('accuracy');
title(t);
pbaspect([4 1 1]);

ax = gca;
outerpos = ax.OuterPosition;
ti = ax.TightInset; 
left = outerpos(1) + ti(1);
bottom = outerpos(2) + ti(2);
ax_width = outerpos(3) - ti(1) - ti(3);
ax_height = outerpos(4) - ti(2) - ti(4);
ax.Position = [left bottom ax_width ax_height];

saveas(gcf,strcat(folder,'\',file,'_','acc'),'eps');

clf('reset')