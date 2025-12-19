FROM nginx:latest

# 复制自定义的 nginx.conf 到容器中的 nginx 配置目录
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制前端构建好的dist文件夹内容到nginx默认静态目录
COPY dist/ /usr/share/nginx/html/
RUN chmod -R 755 /usr/share/nginx/html
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
