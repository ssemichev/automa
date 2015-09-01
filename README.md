# automa
AWS Scala helpers

## Alarms
    DataSourcesWatcherAlarm
    DataSourcesValidatorAlarm


```
# AWS Lambda function
    lambda_function_name=test01
    lambda_function_region=us-east-1
    account=374809787535
    lambda_function_arn="arn:aws:lambda:$lambda_function_region:$account:function:$lambda_function_name"

# Unreliable Town Clock public SNS Topic
    sns_topic_arn=arn:aws:sns:us-east-1:522480313337:unreliable-town-clock-topic-178F1OQACHTYF

# Subscribe the AWS Lambda function to the SNS Topic
    aws sns subscribe \
        --topic-arn "$sns_topic_arn" \
        --protocol lambda \
        --notification-endpoint "$lambda_function_arn"
```

### lambda_dynamo policy
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Stmt1428341300017",
            "Action": [
                "dynamodb:DeleteItem",
                "dynamodb:GetItem",
                "dynamodb:PutItem",
                "dynamodb:Query",
                "dynamodb:Scan",
                "dynamodb:UpdateItem",
                "dynamodb:DescribeTable"
            ],
            "Effect": "Allow",
            "Resource": "*"
        },
        {
            "Sid": "",
            "Resource": "*",
            "Action": [
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Effect": "Allow"
        },
        {
          "Effect": "Allow",
          "Action": [
            "sns:Publish"
          ],
          "Resource": "arn:aws:sns:us-east-1:374809787535:general-non-critical-tvdev-pagerduty"
        }
    ]
}
```
