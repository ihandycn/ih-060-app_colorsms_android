# coding:utf-8
import os
import sys

def start_find(original_path,target_path,key):
	all_file_list = os.listdir(original_path)
	for file_name in all_file_list:
		if file_name.startswith('values'):
			original_file = os.path.join(original_path+'/'+file_name,"strings.xml")
			target_file = os.path.join(target_path+'/'+file_name,"strings.xml")
			if os.path.exists(original_file) and os.path.exists(target_file):
				f = open(original_file,'r')
				for line in f.readlines():
					if key in line:
						targetF = file(target_file)
						tr = targetF.read()
						targetF.close()
						targetLines = tr.split('\n')
						l = targetLines.index('</resources>')-1
						targetLines.insert(l,line)
						s = '\n'.join(targetLines)  
						ta = open(target_file,"w")
						ta.write(s)
						ta.close()
						break
				f.close()

if __name__ == '__main__':
	print sys.argv[1]
	start_find(sys.argv[1],sys.argv[2],'"'+sys.argv[3]+'"')	







