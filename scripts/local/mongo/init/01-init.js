db = db.getSiblingDB('aoxiaoyou_doc');

db.createUser({
  user: 'root',
  pwd: 'root',
  roles: [
    {
      role: 'readWrite',
      db: 'aoxiaoyou_doc'
    }
  ]
});

db.createCollection('README_PLACEHOLDER');
db.README_PLACEHOLDER.insertOne({
  name: 'trip-of-macau-local-mongo',
  description: '当前 admin-backend 还未真实接入 MongoDB，本地先预留同账号密码的文档数据库环境。',
  createdAt: new Date()
});
