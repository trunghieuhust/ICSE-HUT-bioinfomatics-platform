user="admin"
tenant="admin"
password="Bkcloud12@Icse@2015"

#update va cai dat cac goi can thiet
sudo apt-get update
sudo apt-get install git fuse build-essential libcurl4-openssl-dev libxml2-dev libssl-dev libfuse-dev -y

#get and build cloudfuse from source code
git clone https://github.com/redbo/cloudfuse.git
cd cloudfuse
./configure
make
sudo make install
cd ~


#tao file moi truong de login vao swift
cat >~/.cloudfuse <<EOL
username=${user}
tenant=${tenant}
password=${password}
authurl=http://192.168.50.12:5000/v2.0/
EOL

#Tao folder de mount va mount toan bo containers
mkdir swift-data-folder
cloudfuse swift-data-folder
#check folder
ls -l swift-data-folder
