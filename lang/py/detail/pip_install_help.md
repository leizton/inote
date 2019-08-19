# 查看包版本
```py
import numpy
numpy.__version__
```


# 升级pip自身
sudo pip install --upgrade pip


#
ERROR: Cannot uninstall 'scipy'. It is a distutils installed project
加 `--ignore-installed` 选项
`sudo pip install --upgrade --ignore-installed scipy`
