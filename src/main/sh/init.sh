#!/bin/bash

# Create DynamoDb table DataSourcesUpdates
# Create Read Capacity Units and Wriet Capacity Units alarms

# lambda_dynamo policy

#{
#    "Version": "2012-10-17",
#    "Statement": [
#        {
#            "Sid": "Stmt1428341300017",
#            "Action": [
#                "dynamodb:DeleteItem",
#                "dynamodb:GetItem",
#                "dynamodb:PutItem",
#                "dynamodb:Query",
#                "dynamodb:Scan",
#                "dynamodb:UpdateItem",
#                "dynamodb:DescribeTable"
#            ],
#            "Effect": "Allow",
#            "Resource": "*"
#        },
#        {
#            "Sid": "",
#            "Resource": "*",
#            "Action": [
#                "logs:CreateLogGroup",
#                "logs:CreateLogStream",
#                "logs:PutLogEvents"
#            ],
#            "Effect": "Allow"
#        },
#        {
#          "Effect": "Allow",
#          "Action": [
#            "sns:Publish"
#          ],
#          "Resource": "arn:aws:sns:us-east-1:374809787535:general-non-critical-tvdev-pagerduty"
#        }
#    ]
#}

# Create dataSourcesWatcher lambda function
# Add S3 event sources
# Add DataSourcesWatcherAlarm

# Create dataSourcesValidator lambda function
# Add DataSourcesValidatorAlarm
# Subscribe to Unreliable Town Clock public SNS Topic

# AWS Lambda function
    lambda_function_name=<lambda function name>
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


# Sample Notification chime event
#{
#    "Records": [
#        {
#            "EventSource": "aws:sns",
#            "EventVersion": "1.0",
#            "EventSubscriptionArn": "arn:aws:sns:us-east-1:522480313337:unreliable-town-clock-topic-178F1OQACHTYF:18344692-472d-4c05-bc33-314e77cd8d5c",
#            "Sns": {
#                "Type": "Notification",
#                "MessageId": "798fc58d-8d1b-5703-87de-8bd7ffb10c3f",
#                "TopicArn": "arn:aws:sns:us-east-1:522480313337:unreliable-town-clock-topic-178F1OQACHTYF",
#                "Subject": "[Unreliable Town Clock] chime: 2015-08-28 14:30 UTC",
#                "Message": "{\n  \"type\" : \"chime\",\n  \"timestamp\": \"2015-08-28 14:30 UTC\",\n  \"year\": \"2015\",\n  \"month\": \"08\",\n  \"day\": \"28\",\n  \"hour\": \"14\",\n  \"minute\": \"30\",\n  \"day_of_week\": \"Fri\",\n  \"unique_id\": \"6adb6ac7-67fb-4e31-8ab1-c2e589f4e164\",\n  \"region\": \"us-east-1\",\n  \"sns_topic_arn\": \"arn:aws:sns:us-east-1:522480313337:unreliable-town-clock-topic-178F1OQACHTYF\",\n  \"reference\": \"https://github.com/alestic/alestic-unreliable-town-clock\",\n  \"support\": \"Eric Hammond <eric-utc@alestic.com>\",\n  \"disclaimer\": \"UNRELIABLE SERVICE\"\n}",
#                "Timestamp": "2015-08-28T14:30:02.132Z",
#                "SignatureVersion": "1",
#                "Signature": "IjnAjIBXlsTa2vGdxLYh39jt4wMdrk8eYh9ps+YNqBakk45VY5Y2EqVyK5wTRFDwVl0Q/SJitUoBfozkZ+MDcQ5NzKkGl/ifQh+xS+VTFz29A5QlizcraPwS/r9dOUTqJ8MkKXIGgWgQFL8GBd4cfcd2/taJh0sMrVYN933iUon/nehKHRAS2yRmF7oVAA9Z3yBq1ht44Wm4IoXlEeT6Tz9dhoFSKQlzHennJRBN0jzMnehSrM1o+lPaT/VyrMxowKC9Shvqt8Qrbuq9Hjau/bjWO762lGuLYwESrpom2H6rRJpDFn/gzowbH2kMsSLF+msMncUOPVYJdsKvqc7qRQ==",
#                "SigningCertUrl": "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-bb750dd426d95ee9390147a5624348ee.pem",
#                "UnsubscribeUrl": "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:522480313337:unreliable-town-clock-topic-178F1OQACHTYF:18344692-472d-4c05-bc33-314e77cd8d5c",
#                "MessageAttributes": {}
#            }
#        }
#    ]
#}


