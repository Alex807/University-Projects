Implement your "homemade" simplified variant of mpiexec-smpd.   

Implement smpd, a  process manager that can be deployed on different hosts. Smpd acts as a server that receives requests to launch programs. It uses sockets to receive requests. It can launch any program found as an executable file in its local file system.

Implement mpiexec, a client program that transmits requests to smpd servers. It  should be used in the form:

mpiexec -hosts N  IPADDRESS_1 IPADDRESS_2 ....  IPADDRESS_N program.exe

In order to be able to develop and test your implementation on a single computer, have also the scenario when multiple smpd servers are started as distinct processes on different ports on localhost. In this case, mpiexec should be used in this form:

mpiexec -processes N port_1 port_2 .... port_N program.exe

In both cases, there will be created N processes running program.exe and the resulting standard outputs must be shown in the console of mpiexec.