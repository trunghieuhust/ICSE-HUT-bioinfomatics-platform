
#tao file moi truong de login vao swift
cat >~/.cloudfuse <<EOL
username=${user}
tenant=${tenant}
password=${password}
authurl=http://192.168.50.12:5000/v2.0/
EOL

#Tao folder de mount va mount toan bo containers
cloudfuse swift-folder
#check folder
ls -l swift-folder


