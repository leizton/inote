setAddr(char* ip, ushort port, sockaddr_in &addr)
    bzero(&addr, sizeof(addr))
    addr.sin_family = AF_INET
    addr.sin_port = htons(port)  // host to net short
    int nIP = 0  // ip地址对应的整数
    if ip==NULL || *ip=='\0' || ip.equal("0") || ip.equal("0.0.0.0") || ip.equal("*")
        nIP = htonl(INADDR_ANY)  // host to net long(4 bytes)
    else
        nIP = inet_addr(ip)
    addr.sin_addr.s_addr = nIP

setNonBlock(int fd)
    int flag = fcntl(fd, F_GETFL, 0)
    flag |= O_NONBLOCK
    flag |= O_NDELAY
    fcntl(fd, F_SETFL, flag)