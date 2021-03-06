%set to 1 once we use OST4
%may need to rescale times piecewise linear when building the model
timestep=10; %[s]
initialframe=1020;
%this is cell AB, 17 minutes

dat=importdata('/Volumes/TBU_main02/ost4dgood/celegans2008.2.ost/data/volstats.txt');

curframe=(dat(:,1)-initialframe)*timestep + 17*60;
numcell=dat(:,2);

N=curframe./60; %[min]
X=numcell;

%write dat-file
%out=[N,X];
fp=fopen('series.dat','wt');
for i=1:length(N)
    fprintf(fp,'%f\t%f\n',N(i),X(i));
end
fclose(fp);

disp('final number of cells');
numcell(end)